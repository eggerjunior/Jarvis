# 🤖 Jarvis — Assistente Pessoal Inteligente & Second Brain

[![Android](https://img.shields.io/badge/Android-Native-green.svg)](android/)
[![iOS](https://img.shields.io/badge/iOS-SwiftUI-blue.svg)](ios/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](android/app)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg)](android/app)
[![Swift](https://img.shields.io/badge/Language-Swift-FA7343.svg)](ios/)

**Jarvis** é um assistente pessoal por voz de alta performance com inteligência artificial integrada e um ecossistema de **Second Brain (Segundo Cérebro)** para organização pessoal, profissional e de conhecimento.

---

## 🌟 Principais Recursos

### 🎙️ Assistente de Voz & IA Multi-Provedor
- **Reconhecimento & Síntese de Voz**: Reconhecimento continuo de fala em Português e síntese de áudio configurável (masculina, feminina ou automática).
- **Integração com LLMs**: Suporte a provedores de inteligência artificial via API:
  - **Anthropic** (ex: Claude 3.5 Sonnet, Claude 5)
  - **OpenRouter** (Auto / seleção de modelos customizados)
- **Ferramentas Integradas**: Capacidade de busca web em tempo real integrada aos modelos de IA para respostas atualizadas.
- **Ativação por Voz**: Suporte a acionamento mãos-livres ("Ei Jarvis").

### 🧠 Second Brain (Segundo Cérebro)
- Organização em 8 áreas fundamentais: **Metas, Carreira/Trabalho, Projetos, Finanças, Aprendizado, Saúde, Relações e Você**.
- Editor de notas interativo e visualização de conexões entre conhecimentos.
- Contextualização automática para o Jarvis responder com base nas preferências e histórico pessoal.

### 🔒 Segurança & Privacidade
- Armazenamento criptografado de chaves de API (`EncryptedSharedPreferences` com esquema `AES256_SIV` no Android e `Keychain` no iOS).

---

## 📱 Suporte Multi-plataforma

| Plataforma | Tecnologias Principais | Destaques |
| :--- | :--- | :--- |
| **Android** | Kotlin, Jetpack Compose, Material 3, Coroutines, OkHttp, EncryptedSharedPreferences, JUnit 4 | App 100% nativo com animações do orbe do Jarvis, suporte a testes unitários e arquitetura reativa com StateFlow. |
| **iOS / CarPlay** | Swift, SwiftUI, AVFoundation, Speech Framework, CarPlay Template | Integração com Apple CarPlay (Voice Control Template), suporte a fundo de áudio e sessão compartilhada. |
| **Web / Backend** | Python, JavaScript, React, HTML5 | Interface web complementar e utilitários de suporte. |

---

## 🛠️ Como Executar o Projeto Android

### Pré-requisitos
- Android Studio Ladybug (ou versão mais recente)
- JDK 17+
- Dispositivo físico ou Emulador com Android 8.0+ (API level 26+)

### Passos
1. Clone o repositório:
   ```bash
   git clone https://github.com/eggerjunior/Jarvis.git
   cd Jarvis/android
   ```
2. Abra a pasta `android` no Android Studio.
3. Execute o build e rode os testes unitários no terminal do projeto:
   ```bash
   ./gradlew testDebugUnitTest
   ```
4. Compile e execute o aplicativo no dispositivo conectado ou emulador.

---

## 🧪 Testes Unitários (Android)
O projeto conta com suíte de testes unitários automatizados cobrindo os modelos de dados e a máquina de estados do Jarvis:
- `ModelsTest`: Testes de conversão de provedores de IA, preferências de voz, notas do Second Brain e histórico de versões.
- `JarvisSessionTest`: Testes de transição de estados do assistente (`IDLE`, `LISTENING`, `THINKING`, `SPEAKING`, `ERROR`).

Para rodar os testes:
```bash
./gradlew :app:testDebugUnitTest
```

---

## 📄 Licença

Desenvolvido por **Ildemar Egger** (`br.app.egger.jarvis`). Todos os direitos reservados.
