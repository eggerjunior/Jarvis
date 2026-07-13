import AVFoundation
import Foundation
import Speech

@MainActor
protocol JarvisSpeechRecognizerDelegate: AnyObject {
    func speechDidDetectSpeechActivity()
    func speechDidRecognizeFinalText(_ text: String)
}

final class JarvisSpeechRecognizer {
    static let shared = JarvisSpeechRecognizer()

    private enum SilenceTiming {
        static let normalPause: TimeInterval = 3.0
        static let likelyContinuationPause: TimeInterval = 5.0
    }

    weak var delegate: JarvisSpeechRecognizerDelegate?

    private let audioEngine = AVAudioEngine()
    private var recognizer = SFSpeechRecognizer(locale: Locale(identifier: "pt-BR"))
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var task: SFSpeechRecognitionTask?
    private var silenceTimer: Timer?
    private var lastText = ""

    private init() {}

    static func requestPermissions() async -> Bool {
        let speechAllowed = await withCheckedContinuation { continuation in
            SFSpeechRecognizer.requestAuthorization { status in
                continuation.resume(returning: status == .authorized)
            }
        }

        let micAllowed = await withCheckedContinuation { continuation in
            if #available(iOS 17.0, *) {
                AVAudioApplication.requestRecordPermission { continuation.resume(returning: $0) }
            } else {
                AVAudioSession.sharedInstance().requestRecordPermission { continuation.resume(returning: $0) }
            }
        }

        return speechAllowed && micAllowed
    }

    func start() throws {
        stop()

        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.playAndRecord, mode: .spokenAudio, options: [.duckOthers, .defaultToSpeaker])
        try session.setActive(true, options: .notifyOthersOnDeactivation)

        guard let recognizer, recognizer.isAvailable else {
            throw NSError(domain: "JarvisSpeechRecognizer", code: 1, userInfo: [NSLocalizedDescriptionKey: "Reconhecimento de fala indisponível."])
        }

        let request = SFSpeechAudioBufferRecognitionRequest()
        request.shouldReportPartialResults = true
        self.request = request
        lastText = ""

        let input = audioEngine.inputNode
        let format = input.outputFormat(forBus: 0)
        input.removeTap(onBus: 0)
        input.installTap(onBus: 0, bufferSize: 1024, format: format) { [weak self] buffer, _ in
            self?.request?.append(buffer)
        }

        audioEngine.prepare()
        try audioEngine.start()

        task = recognizer.recognitionTask(with: request) { [weak self] result, error in
            guard let self else { return }
            if let result {
                let text = result.bestTranscription.formattedString.trimmingCharacters(in: .whitespacesAndNewlines)
                if !text.isEmpty, text != self.lastText {
                    DispatchQueue.main.async {
                        self.lastText = text
                        self.delegate?.speechDidDetectSpeechActivity()
                        self.resetSilenceTimer()
                    }
                }
            }
            if error != nil {
                DispatchQueue.main.async { self.stop() }
            }
        }
    }

    func stop() {
        silenceTimer?.invalidate()
        silenceTimer = nil
        if audioEngine.isRunning {
            audioEngine.stop()
            audioEngine.inputNode.removeTap(onBus: 0)
        }
        request?.endAudio()
        request = nil
        task?.cancel()
        task = nil
    }

    private func resetSilenceTimer() {
        silenceTimer?.invalidate()
        let text = lastText
        silenceTimer = Timer.scheduledTimer(withTimeInterval: silenceInterval(for: text), repeats: false) { [weak self] _ in
            guard let self else { return }
            let text = self.lastText
            self.lastText = ""
            if !text.isEmpty {
                Task { @MainActor in self.delegate?.speechDidRecognizeFinalText(text) }
            }
        }
    }

    private func silenceInterval(for text: String) -> TimeInterval {
        let normalized = text
            .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
            .lowercased()
            .trimmingCharacters(in: .whitespacesAndNewlines.union(.punctuationCharacters))
        let words = normalized.split(separator: " ").map(String.init)
        guard let lastWord = words.last else { return SilenceTiming.normalPause }

        let continuationWords: Set<String> = [
            "a", "as", "com", "da", "das", "de", "do", "dos", "e", "em", "na", "nas", "no", "nos",
            "o", "os", "para", "por", "que", "qual", "quais", "quem", "sobre"
        ]
        if continuationWords.contains(lastWord) {
            return SilenceTiming.likelyContinuationPause
        }

        return SilenceTiming.normalPause
    }
}
