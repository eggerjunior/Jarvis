import React, { useState, useEffect, useRef } from 'react';

const API_BASE = 'http://localhost:8000';

function App() {
  const [messages, setMessages] = useState([
    {
      role: 'model',
      text: 'Olá! Sou o Jarvis, seu assistente pessoal. Em que posso ser útil hoje?'
    }
  ]);
  const [input, setInput] = useState('');
  const [status, setStatus] = useState('idle'); // idle | listening | thinking | speaking
  const [connected, setConnected] = useState(false);
  const [showConfig, setShowConfig] = useState(false);
  const [apiKey, setApiKey] = useState('');
  const [systemStats, setSystemStats] = useState({
    cpu_percent: 0,
    memory_percent: 0,
    disk_percent: 0
  });

  const chatEndRef = useRef(null);
  const recognitionRef = useRef(null);

  // Initialize Speech Recognition
  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
      const rec = new SpeechRecognition();
      rec.continuous = false;
      rec.lang = 'pt-BR';
      rec.interimResults = false;

      rec.onstart = () => {
        setStatus('listening');
      };

      rec.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        setInput(transcript);
        sendMessage(transcript);
      };

      rec.onerror = (e) => {
        console.error('Speech recognition error:', e);
        setStatus('idle');
      };

      rec.onend = () => {
        // Only reset to idle if we weren't transitioned to thinking
        setStatus(current => current === 'listening' ? 'idle' : current);
      };

      recognitionRef.current = rec;
    }
  }, []);

  // Poll connection & system stats
  useEffect(() => {
    const checkStatus = async () => {
      try {
        const res = await fetch(`${API_BASE}/api/status`);
        if (res.ok) {
          const data = await res.json();
          setConnected(true);
          setSystemStats(data.system_stats);
          if (!data.api_key_configured) {
            setShowConfig(true);
          }
        } else {
          setConnected(false);
        }
      } catch (err) {
        setConnected(false);
      }
    };

    checkStatus();
    const interval = setInterval(checkStatus, 5000);
    return () => clearInterval(interval);
  }, []);

  // Scroll to bottom when messages change
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Voice synthesis helper
  const speakText = (text) => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel(); // Stop any current speech
      
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'pt-BR';
      
      utterance.onstart = () => setStatus('speaking');
      utterance.onend = () => setStatus('idle');
      utterance.onerror = () => setStatus('idle');

      // Attempt to load standard voices
      const voices = window.speechSynthesis.getVoices();
      const ptVoice = voices.find(v => v.lang.startsWith('pt'));
      if (ptVoice) {
        utterance.voice = ptVoice;
      }
      
      window.speechSynthesis.speak(utterance);
    }
  };

  // Submit API key
  const handleSaveConfig = async (e) => {
    e.preventDefault();
    if (!apiKey.trim()) return;

    try {
      const res = await fetch(`${API_BASE}/api/config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ api_key: apiKey })
      });
      if (res.ok) {
        setShowConfig(false);
        setConnected(true);
        speakText("Chave API configurada com sucesso. Estou pronto para ajudar.");
      } else {
        alert("Falha ao salvar a chave API.");
      }
    } catch (err) {
      alert("Erro ao conectar com o servidor.");
    }
  };

  // Send message to assistant
  const sendMessage = async (textToSend) => {
    const messageText = textToSend || input;
    if (!messageText.trim()) return;

    // Append user message
    const updatedMessages = [...messages, { role: 'user', text: messageText }];
    setMessages(updatedMessages);
    setInput('');
    setStatus('thinking');

    try {
      // Build brief history to supply context
      const historyPayload = messages.slice(-6).map(m => ({
        role: m.role,
        text: m.text
      }));

      const res = await fetch(`${API_BASE}/api/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: messageText,
          history: historyPayload
        })
      });

      if (res.ok) {
        const data = await res.json();
        setMessages(prev => [...prev, {
          role: 'model',
          text: data.text,
          actions: data.actions
        }]);
        speakText(data.text);
      } else {
        const errorData = await res.json();
        const errorMessage = errorData.detail || 'Erro ao processar mensagem.';
        setMessages(prev => [...prev, { role: 'model', text: errorMessage }]);
        setStatus('idle');
      }
    } catch (err) {
      setMessages(prev => [...prev, { role: 'model', text: 'Não foi possível conectar com o Jarvis local.' }]);
      setStatus('idle');
    }
  };

  // Toggle Voice Input
  const toggleListening = () => {
    if (status === 'listening') {
      recognitionRef.current?.stop();
    } else {
      // Stop speech synthesis if talking
      if (status === 'speaking') {
        window.speechSynthesis.cancel();
        setStatus('idle');
      }
      recognitionRef.current?.start();
    }
  };

  // Format arguments for action badges
  const formatArgs = (args) => {
    if (!args) return '';
    return Object.entries(args).map(([k, v]) => `${k}: ${v}`).join(', ');
  };

  return (
    <div className="app-container">
      {/* Configuration Modal */}
      {showConfig && (
        <div className="modal-overlay">
          <form className="modal-content" onSubmit={handleSaveConfig}>
            <h2 className="modal-title">Configuração do Jarvis</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Para inicializar o assistente, insira sua chave da API do Gemini. Ela será salva localmente no arquivo <code>backend/.env</code>.
            </p>
            <input
              type="password"
              placeholder="Inserir API Key do Gemini..."
              className="text-input"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              required
            />
            <button type="submit" className="btn-primary">Salvar Chave</button>
          </form>
        </div>
      )}

      {/* Header */}
      <header>
        <div className="logo">
          JARVIS
        </div>
        <div className="system-monitor">
          <div className="stat-card cpu">
            <span className="stat-label">CPU</span>
            <span className="stat-value">{systemStats.cpu_percent}%</span>
          </div>
          <div className="stat-card memory">
            <span className="stat-label">Memória</span>
            <span className="stat-value">{systemStats.memory_percent}%</span>
          </div>
          <div className="stat-card disk">
            <span className="stat-label">Disco</span>
            <span className="stat-value">{systemStats.disk_percent}%</span>
          </div>
        </div>
      </header>

      {/* Workspace */}
      <main className="main-content">
        {/* Arc Reactor Left */}
        <div className="reactor-section">
          <div className={`reactor-outer ${status}`} onClick={toggleListening}>
            <div className="reactor-middle">
              <div className="reactor-inner">
                <div className="reactor-core"></div>
              </div>
            </div>
          </div>
          <div className="status-text">
            {status === 'idle' && 'Sistema Pronto'}
            {status === 'listening' && 'Ouvindo...'}
            {status === 'thinking' && 'Processando...'}
            {status === 'speaking' && 'Transmitindo...'}
          </div>
        </div>

        {/* Chat Log Right */}
        <div className="chat-section">
          <div className="chat-history">
            {messages.map((msg, index) => (
              <div key={index} className={`chat-message ${msg.role === 'user' ? 'user' : 'assistant'}`}>
                <div>{msg.text}</div>
                {msg.actions && msg.actions.map((act, i) => (
                  <div key={i} className="action-badge">
                    <span style={{ fontWeight: 600 }}>🛠️ Executado:</span> {act.name}({formatArgs(act.args)})
                  </div>
                ))}
              </div>
            ))}
            <div ref={chatEndRef} />
          </div>
        </div>
      </main>

      {/* Input Bar */}
      <div className="input-bar">
        <button 
          className={`icon-btn mic-btn ${status === 'listening' ? 'active' : ''}`}
          onClick={toggleListening}
          title={status === 'listening' ? 'Parar captura' : 'Capturar voz'}
        >
          🎙️
        </button>
        <input
          type="text"
          placeholder="Escreva um comando ou converse..."
          className="text-input"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
        />
        <button 
          className="icon-btn send-btn" 
          onClick={() => sendMessage()}
          title="Enviar"
        >
          ⚡
        </button>
      </div>

      {/* Footer */}
      <footer>
        <div className="connection-status">
          <span className={`status-dot ${connected ? 'online' : ''}`}></span>
          <span>{connected ? 'Conectado ao núcleo local' : 'Desconectado do núcleo'}</span>
        </div>
        <div>
          Pressione o Reator ou o Microfone para falar
        </div>
      </footer>
    </div>
  );
}

export default App;
