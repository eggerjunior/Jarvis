package br.app.egger.jarvis.session

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.app.egger.jarvis.model.AIModel
import br.app.egger.jarvis.model.AIProvider
import br.app.egger.jarvis.model.BrainNote
import br.app.egger.jarvis.model.JarvisVoiceOption
import br.app.egger.jarvis.model.JarvisVoicePreference
import br.app.egger.jarvis.model.SecondBrain
import br.app.egger.jarvis.service.AIModelClient
import br.app.egger.jarvis.service.ClaudeMessage
import br.app.egger.jarvis.service.JarvisSpeechRecognizer
import br.app.egger.jarvis.service.JarvisSpeechRecognizerListener
import br.app.egger.jarvis.service.JarvisSpeechSynthesizer
import br.app.egger.jarvis.service.JarvisSpeechSynthesizerListener
import br.app.egger.jarvis.service.SecureStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class JarvisState(val rawValue: String) {
    IDLE("Aguardando ativação"),
    LISTENING("Ouvindo"),
    THINKING("Pensando"),
    SPEAKING("Falando"),
    ERROR("Atenção")
}

class JarvisSession(context: Context) : ViewModel(), JarvisSpeechRecognizerListener, JarvisSpeechSynthesizerListener {

    private val appContext = context.applicationContext
    private val storage = SecureStorage(appContext)
    private val client = AIModelClient()
    private val gson = Gson()

    val recognizer = JarvisSpeechRecognizer(appContext)
    val speaker = JarvisSpeechSynthesizer(appContext)

    // State Flows
    private val _state = MutableStateFlow(JarvisState.IDLE)
    val state: StateFlow<JarvisState> = _state.asStateFlow()

    private val _selectedProvider = MutableStateFlow(AIProvider.fromId(storage.getString("ai_provider", "anthropic")))
    val selectedProvider: StateFlow<AIProvider> = _selectedProvider.asStateFlow()

    private val _anthropicApiKey = MutableStateFlow(storage.getString(AIProvider.ANTHROPIC.keychainKey, ""))
    val anthropicApiKey: StateFlow<String> = _anthropicApiKey.asStateFlow()

    private val _openRouterApiKey = MutableStateFlow(storage.getString(AIProvider.OPEN_ROUTER.keychainKey, ""))
    val openRouterApiKey: StateFlow<String> = _openRouterApiKey.asStateFlow()

    private val _omniRouteApiKey = MutableStateFlow(storage.getString(AIProvider.OMNI_ROUTE.keychainKey, ""))
    val omniRouteApiKey: StateFlow<String> = _omniRouteApiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(
        storage.getString(AIProvider.fromId(storage.getString("ai_provider", "anthropic")).modelDefaultsKey, AIProvider.fromId(storage.getString("ai_provider", "anthropic")).defaultModel)
            .let { saved -> if (saved == "cc/claude-fable-5") AIProvider.OMNI_ROUTE.defaultModel else saved }
    )
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _candidateModel = MutableStateFlow(_selectedModel.value)
    val candidateModel: StateFlow<String> = _candidateModel.asStateFlow()

    private val _availableProviderModels = MutableStateFlow<Map<AIProvider, List<String>>>(emptyMap())
    val availableProviderModels: StateFlow<Map<AIProvider, List<String>>> = _availableProviderModels.asStateFlow()
    private val _isLoadingProviderModels = MutableStateFlow(false)
    val isLoadingProviderModels: StateFlow<Boolean> = _isLoadingProviderModels.asStateFlow()
    private val _modelCatalogError = MutableStateFlow("")
    val modelCatalogError: StateFlow<String> = _modelCatalogError.asStateFlow()

    private val _selectedVoicePreference = MutableStateFlow(JarvisVoicePreference.fromId(storage.getString("jarvis_voice_preference", "masculine")))
    val selectedVoicePreference: StateFlow<JarvisVoicePreference> = _selectedVoicePreference.asStateFlow()

    private val _selectedVoiceIdentifier = MutableStateFlow(storage.getString("jarvis_voice_identifier", ""))
    val selectedVoiceIdentifier: StateFlow<String> = _selectedVoiceIdentifier.asStateFlow()

    private val _userLine = MutableStateFlow("Diga “Ei Jarvis”.")
    val userLine: StateFlow<String> = _userLine.asStateFlow()

    private val _assistantLine = MutableStateFlow("Sistemas prontos.")
    val assistantLine: StateFlow<String> = _assistantLine.asStateFlow()

    private val _modelTestLine = MutableStateFlow("Modelo ainda não testado.")
    val modelTestLine: StateFlow<String> = _modelTestLine.asStateFlow()

    private val _isTestingModel = MutableStateFlow(false)
    val isTestingModel: StateFlow<Boolean> = _isTestingModel.asStateFlow()

    private val _notes = MutableStateFlow(loadNotes())
    val notes: StateFlow<List<BrainNote>> = _notes.asStateFlow()

    private val _isActivated = MutableStateFlow(false)
    val isActivated: StateFlow<Boolean> = _isActivated.asStateFlow()

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    private var messages = mutableListOf<ClaudeMessage>()
    private var handler = Handler(Looper.getMainLooper())
    private var idleRunnable: Runnable? = null
    private var acceptsDirectCommand = false
    private var openFollowUpAfterSpeech = false

    val wakeWord = "ei jarvis"

    companion object {
        val availableModels: List<AIModel> = listOf(
            AIModel("claude-haiku-4-5-20251001", AIProvider.ANTHROPIC, "Claude Haiku 4.5", "$1/$5 por MTok", "Mais barato e mais rápido"),
            AIModel("claude-sonnet-5", AIProvider.ANTHROPIC, "Claude Sonnet 5", "$2/$10 até 31/08/2026", "Equilíbrio custo/inteligência"),
            AIModel("claude-opus-4-8", AIProvider.ANTHROPIC, "Claude Opus 4.8", "$5/$25 por MTok", "Trabalho complexo"),
            AIModel("claude-fable-5", AIProvider.ANTHROPIC, "Claude Fable 5", "$10/$50 por MTok", "Mais capaz e mais caro"),
            AIModel("openrouter/auto", AIProvider.OPEN_ROUTER, "OpenRouter Auto", "roteamento automático", "Escolhe um modelo adequado automaticamente"),
            AIModel("~openai/gpt-latest", AIProvider.OPEN_ROUTER, "OpenAI GPT Latest", "via OpenRouter", "Alias para o GPT flagship mais recente"),
            AIModel("anthropic/claude-sonnet-4.5", AIProvider.OPEN_ROUTER, "Claude Sonnet via OpenRouter", "via OpenRouter", "Claude por agregador"),
            AIModel("google/gemini-2.5-pro", AIProvider.OPEN_ROUTER, "Gemini Pro via OpenRouter", "via OpenRouter", "Google por agregador"),
            AIModel("openai/gpt-4o-mini", AIProvider.OMNI_ROUTE, "GPT-4o Mini", "fallback local", "Atualize para consultar o catálogo do OmniRoute"),
            AIModel("openai/gpt-4o", AIProvider.OMNI_ROUTE, "GPT-4o", "conforme seu plano OmniRoute", "Requer conexão OpenAI ativa no OmniRoute"),
            AIModel("anthropic/claude-sonnet-4.5", AIProvider.OMNI_ROUTE, "Claude Sonnet", "conforme seu plano OmniRoute", "Claude roteado pelo OmniRoute"),
            AIModel("google/gemini-2.5-pro", AIProvider.OMNI_ROUTE, "Gemini Pro", "conforme seu plano OmniRoute", "Gemini roteado pelo OmniRoute"),
            AIModel("moonshotai/kimi-k2", AIProvider.OMNI_ROUTE, "Kimi K2", "conforme seu plano OmniRoute", "Kimi roteado pelo OmniRoute")
        )
    }

    init {
        recognizer.listener = this
        speaker.listener = this
        speaker.voicePreference = _selectedVoicePreference.value
        speaker.selectedVoiceIdentifier = _selectedVoiceIdentifier.value
    }

    fun setSelectedProvider(provider: AIProvider) {
        _selectedProvider.value = provider
        storage.saveString("ai_provider", provider.id)
        if (availableModelsForSelectedProvider().none { it.id == _selectedModel.value }) {
            val defaultM = storage.getString(provider.modelDefaultsKey, provider.defaultModel)
            _selectedModel.value = defaultM
        }
        _candidateModel.value = _selectedModel.value
    }

    fun setAnthropicApiKey(key: String) {
        _anthropicApiKey.value = key
        storage.saveString(AIProvider.ANTHROPIC.keychainKey, key)
    }

    fun setOpenRouterApiKey(key: String) {
        _openRouterApiKey.value = key
        storage.saveString(AIProvider.OPEN_ROUTER.keychainKey, key)
    }

    fun setOmniRouteApiKey(key: String) {
        _omniRouteApiKey.value = key
        storage.saveString(AIProvider.OMNI_ROUTE.keychainKey, key)
    }

    fun refreshSelectedProviderModels() {
        val provider = _selectedProvider.value
        val key = currentApiKey().trim()
        if (key.isEmpty() || _isLoadingProviderModels.value) return
        _isLoadingProviderModels.value = true
        _modelCatalogError.value = ""
        viewModelScope.launch {
            try {
                val models = client.listModels(provider, key)
                _availableProviderModels.value = _availableProviderModels.value + (provider to models)
                if (_candidateModel.value !in models) {
                    _candidateModel.value = _selectedModel.value.takeIf { it in models } ?: models.firstOrNull().orEmpty()
                }
                _modelTestLine.value = "${models.size} modelos acessíveis retornados por ${provider.label}."
            } catch (e: Exception) {
                _modelCatalogError.value = e.message ?: "Erro ao consultar catálogo."
                _modelTestLine.value = "Falha ao listar modelos: ${_modelCatalogError.value}"
            } finally {
                _isLoadingProviderModels.value = false
            }
        }
    }

    fun setCandidateModel(modelId: String) {
        _candidateModel.value = modelId
        _modelTestLine.value = "Modelo $modelId selecionado para teste."
    }

    fun useCandidateModel() {
        setSelectedModel(_candidateModel.value)
        _modelTestLine.value = "${_candidateModel.value} definido como modelo ativo do Jarvis."
    }

    fun setSelectedModel(modelId: String) {
        _selectedModel.value = modelId
        _candidateModel.value = modelId
        storage.saveString(_selectedProvider.value.modelDefaultsKey, modelId)
    }

    fun setSelectedVoicePreference(pref: JarvisVoicePreference) {
        _selectedVoicePreference.value = pref
        storage.saveString("jarvis_voice_preference", pref.id)
        speaker.voicePreference = pref
    }

    fun setSelectedVoiceIdentifier(voiceId: String) {
        _selectedVoiceIdentifier.value = voiceId
        storage.saveString("jarvis_voice_identifier", voiceId)
        speaker.selectedVoiceIdentifier = voiceId
    }

    fun start() {
        if (_isActivated.value || _isStarting.value) return
        _isStarting.value = true

        _permissionsGranted.value = true
        _isStarting.value = false
        _isActivated.value = true
        _state.value = JarvisState.IDLE
        _userLine.value = "Diga “Ei Jarvis”."
        speak("Sistemas online. Diga Ei Jarvis quando precisar, Senhor.", followUp = false)
        restartListening()
    }

    fun stop() {
        _isStarting.value = false
        clearIdleTimer()
        recognizer.stop()
        speaker.stop()
        _state.value = JarvisState.IDLE
        _isActivated.value = false
        _userLine.value = "Sistema em espera."
    }

    fun sendTypedCommand(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        openConversationWindow()
        handleCommand(trimmed)
    }

    fun testSelectedModel() {
        if (_isTestingModel.value) return
        val currentKey = currentApiKey()
        if (currentKey.trim().isEmpty()) {
            _modelTestLine.value = "Cole a API key de ${_selectedProvider.value.label} antes de testar o modelo."
            return
        }

        _isTestingModel.value = true
        _modelTestLine.value = "Testando ${_selectedProvider.value.label} / ${_candidateModel.value}..."

        viewModelScope.launch {
            try {
                val result = client.testModel(_selectedProvider.value, currentKey, _candidateModel.value)
                val usage = "${result.inputTokens ?: "n/d"} entrada / ${result.outputTokens ?: "n/d"} saída"
                _modelTestLine.value = "Provedor: ${_selectedProvider.value.label}\nPedido: ${result.requestedModel}\nResposta API: ${result.responseModel}\nTokens: $usage\nRetorno: ${result.text}"
            } catch (e: Exception) {
                _modelTestLine.value = "Falha no teste: ${classify(e)}"
            } finally {
                _isTestingModel.value = false
            }
        }
    }

    fun testSelectedVoice() {
        speaker.voicePreference = _selectedVoicePreference.value
        speaker.selectedVoiceIdentifier = _selectedVoiceIdentifier.value
        speaker.preview("Senhor, esta é a voz selecionada para o Jarvis.", _selectedVoiceIdentifier.value)
    }

    fun availableVoiceOptions(): List<JarvisVoiceOption> = speaker.getAvailableVoices()

    fun availableModelsForSelectedProvider(): List<AIModel> {
        val provider = _selectedProvider.value
        return _availableProviderModels.value[provider].orEmpty().map { id ->
            availableModels.firstOrNull { it.provider == provider && it.id == id }
                ?: AIModel(id, provider, id, "disponível na API", "Retornado pelo catálogo autenticado")
        }
    }

    fun currentApiKey(): String {
        return when (_selectedProvider.value) {
            AIProvider.ANTHROPIC -> _anthropicApiKey.value
            AIProvider.OPEN_ROUTER -> _openRouterApiKey.value
            AIProvider.OMNI_ROUTE -> _omniRouteApiKey.value
        }
    }

    private fun restartListening() {
        if (!_isActivated.value || _state.value == JarvisState.SPEAKING || _state.value == JarvisState.THINKING) return
        try {
            recognizer.start()
            _state.value = JarvisState.LISTENING
        } catch (e: Exception) {
            _state.value = JarvisState.ERROR
            _assistantLine.value = "Não consegui iniciar a escuta. Verifique a permissão do microfone e tente novamente."
        }
    }

    private fun handleRecognized(text: String) {
        if (!_isActivated.value || _state.value == JarvisState.SPEAKING || _state.value == JarvisState.THINKING) return
        val normalized = normalize(text)

        if (acceptsDirectCommand) {
            clearIdleTimer()
            handleCommand(text)
            return
        }

        val wakeRangeIndex = normalized.indexOf(wakeWord).takeIf { it >= 0 } ?: normalized.indexOf("jarvis").takeIf { it >= 0 }

        if (wakeRangeIndex == null) {
            _userLine.value = "Ouvi: “$text”. Aguardando “Ei Jarvis”."
            _state.value = JarvisState.IDLE
            return
        }

        openConversationWindow()
        val commandStart = wakeRangeIndex + (if (normalized.contains(wakeWord)) wakeWord.length else "jarvis".length)
        val command = if (commandStart < text.length) text.substring(commandStart).trim() else ""

        if (command.isEmpty()) {
            _userLine.value = "Ativado. Pode falar agora."
        } else {
            handleCommand(command)
        }
    }

    private fun handleCommand(text: String) {
        val requestedWebSearch = shouldUseWebSearch(text)
        val provider = if (requestedWebSearch) AIProvider.OPEN_ROUTER else _selectedProvider.value
        val apiKey = apiKeyFor(provider)
        val model = modelFor(provider)

        if (apiKey.trim().isEmpty()) {
            if (requestedWebSearch) {
                speak("Para pesquisar atualidades na internet, cole sua API key do OpenRouter nos ajustes, Senhor.", followUp = true)
            } else {
                speak("Cole sua API key de ${provider.label} nos ajustes superiores, Senhor.", followUp = true)
            }
            return
        }

        clearIdleTimer()
        recognizer.stop()
        _state.value = JarvisState.THINKING
        _userLine.value = text
        if (requestedWebSearch) {
            _assistantLine.value = "Pesquisando informações atuais..."
        }
        messages.add(ClaudeMessage("user", text))

        viewModelScope.launch {
            try {
                var answer = client.send(
                    provider = provider,
                    apiKey = apiKey,
                    model = model,
                    system = systemPrompt(webSearchEnabled = requestedWebSearch),
                    messages = messages,
                    enableWebSearch = requestedWebSearch
                )
                answer = applyMemorySave(answer)
                messages.add(ClaudeMessage("assistant", answer))
                _assistantLine.value = answer
                speak(answer, followUp = true)
            } catch (e: Exception) {
                val message = classify(e, provider)
                _assistantLine.value = message
                speak(message, followUp = true)
            }
        }
    }

    private fun openConversationWindow() {
        clearIdleTimer()
        acceptsDirectCommand = true
        idleRunnable = Runnable {
            acceptsDirectCommand = false
            _state.value = JarvisState.IDLE
            _userLine.value = "Diga “Ei Jarvis”."
        }
        handler.postDelayed(idleRunnable!!, 5000L)
    }

    private fun clearIdleTimer() {
        idleRunnable?.let { handler.removeCallbacks(it) }
        idleRunnable = null
        acceptsDirectCommand = false
    }

    private fun holdConversationWindowForDetectedSpeech() {
        if (!acceptsDirectCommand) return
        idleRunnable?.let { handler.removeCallbacks(it) }
        idleRunnable = null
        _userLine.value = "Ouvindo sua resposta..."
    }

    fun updateNote(note: BrainNote) {
        val currentList = _notes.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            currentList[index] = note
            _notes.value = currentList
            saveNotes(currentList)
        }
    }

    private fun speak(text: String, followUp: Boolean) {
        openFollowUpAfterSpeech = followUp
        _state.value = JarvisState.SPEAKING
        recognizer.stop()
        speaker.speak(text)
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .trim()
    }

    private fun systemPrompt(webSearchEnabled: Boolean = false): String {
        val grouped = SecondBrain.areas.keys.sorted().mapNotNull { key ->
            val areaNotes = _notes.value.filter { it.area == key }
            if (areaNotes.isEmpty()) null
            else {
                val label = SecondBrain.areas[key]?.label ?: key
                val body = areaNotes.joinToString("\n") { "- ${it.title}: ${it.body}" }
                "$label:\n$body"
            }
        }.joinToString("\n\n")

        val currentDate = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")).format(Date())

        val searchInstruction = if (webSearchEnabled) {
            "Você tem busca web habilitada nesta resposta. Use-a para fatos recentes, notícias, datas, preços, versões, pessoas públicas e qualquer informação temporal. Cite a fonte de forma curta quando ela fundamentar a resposta."
        } else {
            "Você não tem busca web nesta resposta. Se a pergunta depender de atualidades ou internet e você não tiver busca habilitada, diga isso de forma curta e peça para usar o provedor OpenRouter."
        }

        return """
        Você é Jarvis, um assistente pessoal com personalidade Formal Britânico. Trate o usuário como Senhor.
        Responda sempre em português do Brasil, em tom falado, curto, útil e elegante. Use 2 a 4 frases. Não use emojis nem markdown.
        Data atual: $currentDate.
        $searchInstruction

        SECOND BRAIN COMPLETO:
        $grouped

        Memória viva: Se o usuário revelar algo novo e duradouro, TERMINE a resposta com uma linha no formato EXATO [[SAVE:area|titulo|texto]] (area ∈ metas, trabalho, projetos, financas, aprendizado, saude, relacoes, meta). Se já existir nota com esse título, ela é atualizada; senão nasce uma nova. Inclua só quando houver algo realmente novo.
        """.trimIndent()
    }

    private fun applyMemorySave(answer: String): String {
        val regex = Regex("\\[\\[SAVE:([a-z_]+)\\|([^|]+)\\|([\\s\\S]+?)\\]\\]")
        val match = regex.find(answer) ?: return answer.trim()

        val rawArea = match.groupValues[1]
        val area = if (SecondBrain.areas.containsKey(rawArea)) rawArea else "meta"
        val title = match.groupValues[2].trim()
        val body = match.groupValues[3].trim()

        val currentList = _notes.value.toMutableList()
        val index = currentList.indexOfFirst { normalize(it.title) == normalize(title) }

        if (index >= 0) {
            currentList[index] = currentList[index].copy(area = area, body = body)
        } else {
            currentList.add(BrainNote("$area-${UUID.randomUUID()}", area, title, body))
        }

        _notes.value = currentList
        saveNotes(currentList)

        return regex.replace(answer, "").trim()
    }

    private fun classify(error: Exception, provider: AIProvider? = null): String {
        val message = error.message ?: ""
        if (message.contains("401")) return "Chave inválida ou não autorizada, Senhor."
        if (message.contains("403")) return "A chave não tem permissão para esse recurso, Senhor."
        if (message.contains("429")) return "Limite atingido ou crédito insuficiente em ${provider?.label ?: "seu provedor"}, Senhor."
        if (message.lowercase().contains("credit") || message.lowercase().contains("balance")) {
            return "A chave parece válida, mas falta crédito ou saldo em ${provider?.label ?: "seu provedor"}, Senhor."
        }
        return "Não consegui conectar ao provedor de IA agora, Senhor."
    }

    private fun apiKeyFor(provider: AIProvider): String {
        return when (provider) {
            AIProvider.ANTHROPIC -> _anthropicApiKey.value
            AIProvider.OPEN_ROUTER -> _openRouterApiKey.value
            AIProvider.OMNI_ROUTE -> _omniRouteApiKey.value
        }
    }

    private fun modelFor(provider: AIProvider): String {
        return when (provider) {
            AIProvider.ANTHROPIC -> storage.getString(AIProvider.ANTHROPIC.modelDefaultsKey, AIProvider.ANTHROPIC.defaultModel)
            AIProvider.OPEN_ROUTER -> storage.getString(AIProvider.OPEN_ROUTER.modelDefaultsKey, AIProvider.OPEN_ROUTER.defaultModel)
            AIProvider.OMNI_ROUTE -> storage.getString(AIProvider.OMNI_ROUTE.modelDefaultsKey, AIProvider.OMNI_ROUTE.defaultModel)
                .let { saved -> if (saved == "cc/claude-fable-5") AIProvider.OMNI_ROUTE.defaultModel else saved }
        }
    }

    private fun shouldUseWebSearch(text: String): Boolean {
        val normalized = normalize(text)
        val triggers = listOf(
            "atual", "atuais", "atualidade", "atualizado", "atualizada", "agora", "hoje", "ontem", "amanha",
            "noticia", "noticias", "pesquisa", "pesquisar", "internet", "web", "google", "busca", "buscar",
            "preco", "cotacao", "versao mais recente", "ultimo", "ultima", "lancamento", "tempo real",
            "o que voce sabe", "quem e", "qual e", "me fale sobre"
        )
        return triggers.any { normalized.contains(it) }
    }

    private fun saveNotes(notesList: List<BrainNote>) {
        val json = gson.toJson(notesList)
        storage.saveString("jarvis_notes", json)
    }

    private fun loadNotes(): List<BrainNote> {
        val json = storage.getString("jarvis_notes", "")
        if (json.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<BrainNote>>() {}.type
                val decoded: List<BrainNote>? = gson.fromJson(json, type)
                if (!decoded.isNullOrEmpty()) return decoded
            } catch (e: Exception) {
                // fallback
            }
        }
        return SecondBrain.initialNotes
    }

    // Speech Recognizer Callbacks
    override fun onSpeechDetected() {
        holdConversationWindowForDetectedSpeech()
    }

    override fun onFinalText(text: String) {
        handleRecognized(text)
    }

    override fun onError(errorMessage: String) {
        if (!_isActivated.value || _state.value != JarvisState.LISTENING) return
        _state.value = JarvisState.ERROR
        _assistantLine.value = when {
            errorMessage.contains("indisponível", ignoreCase = true) ->
                "O reconhecimento de voz não está disponível neste dispositivo."
            errorMessage.contains("permissão", ignoreCase = true) ->
                "Permita o acesso ao microfone para conversar com o Jarvis."
            else -> "Tive um problema para ouvir. Toque em Ativar e tente novamente."
        }
    }

    // Speech Synthesizer Callbacks
    override fun onSpeechStarted() {
        _state.value = JarvisState.SPEAKING
    }

    override fun onSpeechFinished() {
        if (!_isActivated.value) return
        _state.value = JarvisState.LISTENING
        if (openFollowUpAfterSpeech) {
            openConversationWindow()
            _userLine.value = "Pode responder agora, Senhor."
        } else {
            clearIdleTimer()
            _userLine.value = "Diga “Ei Jarvis”."
        }
        openFollowUpAfterSpeech = false
        restartListening()
    }
}
