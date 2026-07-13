import Foundation
import SwiftUI

enum AIProvider: String, CaseIterable, Identifiable {
    case anthropic
    case openRouter

    var id: String { rawValue }

    var label: String {
        switch self {
        case .anthropic: return "Anthropic"
        case .openRouter: return "OpenRouter"
        }
    }

    var keychainKey: String {
        switch self {
        case .anthropic: return "anthropic_key"
        case .openRouter: return "openrouter_key"
        }
    }

    var modelDefaultsKey: String {
        switch self {
        case .anthropic: return "anthropic_model"
        case .openRouter: return "openrouter_model"
        }
    }

    var defaultModel: String {
        switch self {
        case .anthropic: return "claude-sonnet-5"
        case .openRouter: return "openrouter/auto"
        }
    }

    var apiKeyPlaceholder: String {
        switch self {
        case .anthropic: return "Anthropic API key"
        case .openRouter: return "OpenRouter API key"
        }
    }
}

@MainActor
final class JarvisSession: ObservableObject {
    struct AIModel: Identifiable, Hashable {
        let id: String
        let provider: AIProvider
        let label: String
        let price: String
        let note: String
    }

    static let availableModels: [AIModel] = [
        .init(id: "claude-haiku-4-5-20251001", provider: .anthropic, label: "Claude Haiku 4.5", price: "$1/$5 por MTok", note: "Mais barato e mais rápido"),
        .init(id: "claude-sonnet-5", provider: .anthropic, label: "Claude Sonnet 5", price: "$2/$10 até 31/08/2026", note: "Equilíbrio custo/inteligência"),
        .init(id: "claude-opus-4-8", provider: .anthropic, label: "Claude Opus 4.8", price: "$5/$25 por MTok", note: "Trabalho complexo"),
        .init(id: "claude-fable-5", provider: .anthropic, label: "Claude Fable 5", price: "$10/$50 por MTok", note: "Mais capaz e mais caro"),
        .init(id: "openrouter/auto", provider: .openRouter, label: "OpenRouter Auto", price: "roteamento automático", note: "Escolhe um modelo adequado automaticamente"),
        .init(id: "~openai/gpt-latest", provider: .openRouter, label: "OpenAI GPT Latest", price: "via OpenRouter", note: "Alias para o GPT flagship mais recente"),
        .init(id: "anthropic/claude-sonnet-4.5", provider: .openRouter, label: "Claude Sonnet via OpenRouter", price: "via OpenRouter", note: "Claude por agregador"),
        .init(id: "google/gemini-2.5-pro", provider: .openRouter, label: "Gemini Pro via OpenRouter", price: "via OpenRouter", note: "Google por agregador")
    ]


    enum State: String {
        case idle = "Aguardando ativação"
        case listening = "Ouvindo"
        case thinking = "Pensando"
        case speaking = "Falando"
        case error = "Atenção"
    }

    @Published var state: State = .idle
    @Published var selectedProvider: AIProvider = AIProvider(rawValue: UserDefaults.standard.string(forKey: "ai_provider") ?? "") ?? .anthropic {
        didSet {
            UserDefaults.standard.set(selectedProvider.rawValue, forKey: "ai_provider")
            if !availableModelsForSelectedProvider.contains(where: { $0.id == selectedModel }) {
                selectedModel = UserDefaults.standard.string(forKey: selectedProvider.modelDefaultsKey) ?? selectedProvider.defaultModel
            }
        }
    }
    @Published var anthropicApiKey: String = KeychainStore.shared.get(key: AIProvider.anthropic.keychainKey) {
        didSet { KeychainStore.shared.save(key: AIProvider.anthropic.keychainKey, value: anthropicApiKey) }
    }
    @Published var openRouterApiKey: String = KeychainStore.shared.get(key: AIProvider.openRouter.keychainKey) {
        didSet { KeychainStore.shared.save(key: AIProvider.openRouter.keychainKey, value: openRouterApiKey) }
    }
    @Published var selectedModel: String = UserDefaults.standard.string(forKey: "anthropic_model") ?? AIProvider.anthropic.defaultModel {
        didSet { UserDefaults.standard.set(selectedModel, forKey: selectedProvider.modelDefaultsKey) }
    }
    @Published var selectedVoicePreference: JarvisVoicePreference = JarvisVoicePreference(rawValue: UserDefaults.standard.string(forKey: "jarvis_voice_preference") ?? "") ?? .masculine {
        didSet {
            UserDefaults.standard.set(selectedVoicePreference.rawValue, forKey: "jarvis_voice_preference")
            speaker.voicePreference = selectedVoicePreference
        }
    }
    @Published var userLine = "Diga “Ei Jarvis”."
    @Published var assistantLine = "Sistemas prontos."
    @Published var modelTestLine = "Modelo ainda não testado."
    @Published var isTestingModel = false
    @Published var notes: [BrainNote] = JarvisSession.loadNotes()
    @Published var isActivated = false
    @Published var permissionsGranted = false

    private let client = AIModelClient()
    private let recognizer = JarvisSpeechRecognizer.shared
    private let speaker = JarvisSpeechSynthesizer.shared
    private var messages: [ClaudeMessage] = []
    private var idleTimer: Timer?
    private var acceptsDirectCommand = false
    private var openFollowUpAfterSpeech = false

    let wakeWord = "ei jarvis"

    init() {
        recognizer.delegate = self
        speaker.delegate = self
        speaker.voicePreference = selectedVoicePreference
        if !availableModelsForSelectedProvider.contains(where: { $0.id == selectedModel }) {
            selectedModel = UserDefaults.standard.string(forKey: selectedProvider.modelDefaultsKey) ?? selectedProvider.defaultModel
        }
    }

    func start() {
        Task {
            permissionsGranted = await JarvisSpeechRecognizer.requestPermissions()
            guard permissionsGranted else {
                state = .error
                assistantLine = "Permita microfone e reconhecimento de fala nos Ajustes do iOS."
                return
            }
            isActivated = true
            state = .idle
            userLine = "Diga “Ei Jarvis”."
            speak("Sistemas online. Diga Ei Jarvis quando precisar, Senhor.", followUp: false)
            restartListening()
        }
    }

    func stop() {
        clearIdleTimer()
        recognizer.stop()
        speaker.stop()
        state = .idle
        isActivated = false
        userLine = "Sistema em espera."
    }

    func sendTypedCommand(_ text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        openConversationWindow()
        handleCommand(trimmed)
    }

    func testSelectedModel() {
        guard !isTestingModel else { return }
        guard !currentApiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            modelTestLine = "Cole a API key de \(selectedProvider.label) antes de testar o modelo."
            return
        }

        isTestingModel = true
        modelTestLine = "Testando \(selectedProvider.label) / \(selectedModel)..."

        Task {
            defer { isTestingModel = false }
            do {
                let result = try await client.testModel(provider: selectedProvider, apiKey: currentApiKey, model: selectedModel)
                let usage = tokenSummary(input: result.inputTokens, output: result.outputTokens)
                modelTestLine = "Provedor: \(selectedProvider.label)\nPedido: \(result.requestedModel)\nResposta API: \(result.responseModel)\nTokens: \(usage)\nRetorno: \(result.text)"
            } catch {
                modelTestLine = "Falha no teste: \(classify(error))"
            }
        }
    }

    var availableModelsForSelectedProvider: [AIModel] {
        Self.availableModels.filter { $0.provider == selectedProvider }
    }

    var currentApiKey: String {
        switch selectedProvider {
        case .anthropic: return anthropicApiKey
        case .openRouter: return openRouterApiKey
        }
    }

    private func restartListening() {
        guard isActivated, state != .speaking, state != .thinking else { return }
        do {
            try recognizer.start()
            state = .listening
        } catch {
            state = .error
            assistantLine = error.localizedDescription
        }
    }

    private func handleRecognized(_ text: String) {
        guard isActivated, state != .speaking, state != .thinking else { return }
        let normalized = normalize(text)

        if directWindowIsOpen {
            clearIdleTimer()
            handleCommand(text)
            return
        }

        guard let wakeRange = normalized.range(of: wakeWord) ?? normalized.range(of: "jarvis") else {
            userLine = "Ouvi: “\(text)”. Aguardando “Ei Jarvis”."
            state = .idle
            return
        }

        openConversationWindow()
        let commandStart = wakeRange.upperBound.utf16Offset(in: normalized)
        let original = Array(text)
        let command = commandStart < original.count ? String(original[commandStart...]).trimmingCharacters(in: .whitespacesAndNewlines) : ""
        if command.isEmpty {
            userLine = "Ativado. Fale em até 2 segundos."
        } else {
            handleCommand(command)
        }
    }

    private func handleCommand(_ text: String) {
        guard !currentApiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            speak("Cole sua API key de \(selectedProvider.label) nos ajustes superiores, Senhor.", followUp: true)
            return
        }

        clearIdleTimer()
        recognizer.stop()
        state = .thinking
        userLine = text
        messages.append(.init(role: "user", content: text))

        Task {
            do {
                var answer = try await client.send(provider: selectedProvider, apiKey: currentApiKey, model: selectedModel, system: systemPrompt(), messages: messages)
                answer = applyMemorySave(answer)
                messages.append(.init(role: "assistant", content: answer))
                assistantLine = answer
                speak(answer, followUp: true)
            } catch {
                let message = classify(error)
                assistantLine = message
                speak(message, followUp: true)
            }
        }
    }

    private var directWindowIsOpen: Bool {
        acceptsDirectCommand
    }

    private func openConversationWindow() {
        clearIdleTimer()
        acceptsDirectCommand = true
        idleTimer = Timer.scheduledTimer(withTimeInterval: 2.0, repeats: false) { [weak self] _ in
            Task { @MainActor in
                self?.idleTimer = nil
                self?.acceptsDirectCommand = false
                self?.state = .idle
                self?.userLine = "Diga “Ei Jarvis”."
            }
        }
    }

    private func clearIdleTimer() {
        idleTimer?.invalidate()
        idleTimer = nil
        acceptsDirectCommand = false
    }

    private func holdConversationWindowForDetectedSpeech() {
        guard acceptsDirectCommand else { return }
        idleTimer?.invalidate()
        idleTimer = nil
        userLine = "Ouvindo sua resposta..."
    }

    func updateNote(_ note: BrainNote) {
        guard let index = notes.firstIndex(where: { $0.id == note.id }) else { return }
        notes[index] = note
        saveNotes()
    }

    private func speak(_ text: String, followUp: Bool) {
        openFollowUpAfterSpeech = followUp
        state = .speaking
        recognizer.stop()
        speaker.speak(text)
    }

    private func normalize(_ value: String) -> String {
        value.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
            .lowercased()
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private func systemPrompt() -> String {
        let grouped = SecondBrain.areas.keys.sorted().compactMap { key -> String? in
            let areaNotes = notes.filter { $0.area == key }
            guard !areaNotes.isEmpty else { return nil }
            let label = SecondBrain.areas[key]?.label ?? key
            let body = areaNotes.map { "- \($0.title): \($0.body)" }.joined(separator: "\n")
            return "\(label):\n\(body)"
        }.joined(separator: "\n\n")

        return """
        Você é Jarvis, um assistente pessoal com personalidade Formal Britânico. Trate o usuário como Senhor.
        Responda sempre em português do Brasil, em tom falado, curto, útil e elegante. Use 2 a 4 frases. Não use emojis nem markdown.

        SECOND BRAIN COMPLETO:
        \(grouped)

        Memória viva: Se o usuário revelar algo novo e duradouro, TERMINE a resposta com uma linha no formato EXATO [[SAVE:area|titulo|texto]] (area ∈ metas, trabalho, projetos, financas, aprendizado, saude, relacoes, meta). Se já existir nota com esse título, ela é atualizada; senão nasce uma nova. Inclua só quando houver algo realmente novo.
        """
    }

    private func applyMemorySave(_ answer: String) -> String {
        guard let regex = try? NSRegularExpression(pattern: #"\[\[SAVE:([a-z_]+)\|([^|]+)\|([\s\S]+?)\]\]"#),
              let match = regex.firstMatch(in: answer, range: NSRange(answer.startIndex..., in: answer)),
              let areaRange = Range(match.range(at: 1), in: answer),
              let titleRange = Range(match.range(at: 2), in: answer),
              let bodyRange = Range(match.range(at: 3), in: answer) else {
            return answer.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let area = SecondBrain.areas[String(answer[areaRange])] == nil ? "meta" : String(answer[areaRange])
        let title = String(answer[titleRange]).trimmingCharacters(in: .whitespacesAndNewlines)
        let body = String(answer[bodyRange]).trimmingCharacters(in: .whitespacesAndNewlines)

        if let index = notes.firstIndex(where: { normalize($0.title) == normalize(title) }) {
            notes[index].area = area
            notes[index].body = body
        } else {
            notes.append(.init(id: "\(area)-\(UUID().uuidString)", area: area, title: title, body: body))
        }
        saveNotes()
        let clean = regex.stringByReplacingMatches(in: answer, range: NSRange(answer.startIndex..., in: answer), withTemplate: "")
        return clean.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private func classify(_ error: Error) -> String {
        let ns = error as NSError
        if ns.code == 401 { return "Chave inválida ou não autorizada, Senhor." }
        if ns.code == 403 { return "A chave não tem permissão para esse recurso, Senhor." }
        if ns.code == 429 { return "Limite atingido ou crédito insuficiente na Anthropic, Senhor." }
        let message = ns.localizedDescription
        if message.lowercased().contains("credit") || message.lowercased().contains("balance") {
            return "A chave parece válida, mas falta crédito ou saldo na Anthropic, Senhor."
        }
        return "Não consegui conectar ao provedor de IA agora, Senhor."
    }

    private func tokenSummary(input: Int?, output: Int?) -> String {
        let inputText = input.map(String.init) ?? "n/d"
        let outputText = output.map(String.init) ?? "n/d"
        return "\(inputText) entrada / \(outputText) saída"
    }

    private func saveNotes() {
        if let data = try? JSONEncoder().encode(notes) {
            UserDefaults.standard.set(data, forKey: "jarvis_notes")
        }
    }

    private static func loadNotes() -> [BrainNote] {
        guard let data = UserDefaults.standard.data(forKey: "jarvis_notes"),
              let decoded = try? JSONDecoder().decode([BrainNote].self, from: data),
              !decoded.isEmpty else {
            return SecondBrain.initialNotes
        }
        return decoded
    }
}

extension JarvisSession: JarvisSpeechRecognizerDelegate {
    func speechDidDetectSpeechActivity() {
        holdConversationWindowForDetectedSpeech()
    }

    func speechDidRecognizeFinalText(_ text: String) {
        handleRecognized(text)
    }
}

extension JarvisSession: JarvisSpeechSynthesizerDelegate {
    func speechDidStart() {
        state = .speaking
    }

    func speechDidFinish() {
        guard isActivated else { return }
        state = .listening
        if openFollowUpAfterSpeech {
            openConversationWindow()
            userLine = "Responda em até 2 segundos ou diga “Ei Jarvis”."
        } else {
            clearIdleTimer()
            userLine = "Diga “Ei Jarvis”."
        }
        openFollowUpAfterSpeech = false
        restartListening()
    }
}
