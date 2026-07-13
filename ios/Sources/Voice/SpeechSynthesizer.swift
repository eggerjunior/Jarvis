import AVFoundation
import Foundation

enum JarvisVoicePreference: String, CaseIterable, Identifiable {
    case masculine
    case automatic
    case feminine

    var id: String { rawValue }

    var label: String {
        switch self {
        case .masculine: return "Masculina"
        case .automatic: return "Automática"
        case .feminine: return "Feminina"
        }
    }
}

struct JarvisVoiceOption: Identifiable, Hashable {
    let id: String
    let name: String
    let language: String
    let gender: String
    let quality: String

    var label: String {
        "\(name) · \(language) · \(gender) · \(quality)"
    }
}

@MainActor
protocol JarvisSpeechSynthesizerDelegate: AnyObject {
    func speechDidStart()
    func speechDidFinish()
}

final class JarvisSpeechSynthesizer: NSObject, AVSpeechSynthesizerDelegate, @unchecked Sendable {
    static let shared = JarvisSpeechSynthesizer()

    private let synthesizer = AVSpeechSynthesizer()
    weak var delegate: JarvisSpeechSynthesizerDelegate?
    var voicePreference: JarvisVoicePreference = .masculine
    var selectedVoiceIdentifier: String?

    static var availableVoiceOptions: [JarvisVoiceOption] {
        AVSpeechSynthesisVoice.speechVoices()
            .sorted { left, right in
                if left.language.hasPrefix("pt") != right.language.hasPrefix("pt") {
                    return left.language.hasPrefix("pt")
                }
                if left.language != right.language {
                    return left.language < right.language
                }
                return left.name.localizedCaseInsensitiveCompare(right.name) == .orderedAscending
            }
            .map {
                JarvisVoiceOption(
                    id: $0.identifier,
                    name: $0.name,
                    language: $0.language,
                    gender: genderLabel($0.gender),
                    quality: qualityLabel($0.quality)
                )
            }
    }

    private override init() {
        super.init()
        synthesizer.delegate = self
    }

    func speak(_ text: String) {
        prepareAudioSession()
        if synthesizer.isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
        }
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = selectedVoice()
        applyProsody(to: utterance)
        synthesizer.speak(utterance)
    }

    func preview(_ text: String, voiceIdentifier: String?) {
        prepareAudioSession()
        if synthesizer.isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
        }
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = explicitVoice(for: voiceIdentifier) ?? selectedVoice()
        applyProsody(to: utterance)
        synthesizer.speak(utterance)
    }

    func stop() {
        synthesizer.stopSpeaking(at: .immediate)
    }

    private func selectedVoice() -> AVSpeechSynthesisVoice? {
        let voices = AVSpeechSynthesisVoice.speechVoices()
        if let explicit = explicitVoice(for: selectedVoiceIdentifier) {
            return explicit
        }

        switch voicePreference {
        case .automatic:
            return AVSpeechSynthesisVoice(language: "pt-BR")
        case .masculine:
            return voiceByIdentifier([
                "com.apple.ttsbundle.Felipe-compact",
                "com.apple.ttsbundle.Felipe-premium",
                "com.apple.ttsbundle.Thiago-compact",
                "com.apple.ttsbundle.Thiago-premium",
                "com.apple.ttsbundle.Joao-compact",
                "com.apple.ttsbundle.Joao-premium",
                "com.apple.ttsbundle.Daniel-compact"
            ])
                ?? namedVoice(in: voices, preferredNames: ["Felipe", "Thiago", "Joao", "João", "Daniel"])
                ?? preferredVoice(in: voices, gender: .male)
                ?? AVSpeechSynthesisVoice(language: "pt-BR")
        case .feminine:
            return voiceByIdentifier([
                "com.apple.ttsbundle.Luciana-compact",
                "com.apple.ttsbundle.Luciana-premium",
                "com.apple.ttsbundle.Fernanda-compact",
                "com.apple.ttsbundle.Maria-compact"
            ])
                ?? namedVoice(in: voices, preferredNames: ["Luciana", "Fernanda", "Mariana", "Maria"])
                ?? preferredVoice(in: voices, gender: .female)
                ?? AVSpeechSynthesisVoice(language: "pt-BR")
        }
    }

    private func prepareAudioSession() {
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(.playAndRecord, mode: .spokenAudio, options: [.duckOthers, .defaultToSpeaker])
            try session.setActive(true, options: .notifyOthersOnDeactivation)
        } catch {
            // AVSpeechSynthesizer can still use the default route if the session setup fails.
        }
    }

    private func applyProsody(to utterance: AVSpeechUtterance) {
        switch voicePreference {
        case .masculine:
            utterance.rate = 0.43
            utterance.pitchMultiplier = 0.50
        case .automatic:
            utterance.rate = 0.48
            utterance.pitchMultiplier = 0.90
        case .feminine:
            utterance.rate = 0.50
            utterance.pitchMultiplier = 1.12
        }
        utterance.volume = 1.0
    }

    private func voiceByIdentifier(_ identifiers: [String]) -> AVSpeechSynthesisVoice? {
        for identifier in identifiers {
            if let voice = AVSpeechSynthesisVoice(identifier: identifier) {
                return voice
            }
        }
        return nil
    }

    private func explicitVoice(for identifier: String?) -> AVSpeechSynthesisVoice? {
        guard let identifier, !identifier.isEmpty else { return nil }
        return AVSpeechSynthesisVoice(identifier: identifier)
    }

    private func namedVoice(in voices: [AVSpeechSynthesisVoice], preferredNames: [String]) -> AVSpeechSynthesisVoice? {
        for name in preferredNames {
            if let voice = voices.first(where: { $0.language.hasPrefix("pt") && ($0.name.localizedCaseInsensitiveContains(name) || $0.identifier.localizedCaseInsensitiveContains(name)) }) {
                return voice
            }
        }
        return nil
    }

    private func preferredVoice(in voices: [AVSpeechSynthesisVoice], gender: AVSpeechSynthesisVoiceGender) -> AVSpeechSynthesisVoice? {
        voices.first { $0.language == "pt-BR" && $0.gender == gender }
            ?? voices.first { $0.language.hasPrefix("pt-") && $0.gender == gender }
    }

    private static func genderLabel(_ gender: AVSpeechSynthesisVoiceGender) -> String {
        switch gender {
        case .male: return "Masculina"
        case .female: return "Feminina"
        case .unspecified: return "Indefinida"
        @unknown default: return "Indefinida"
        }
    }

    private static func qualityLabel(_ quality: AVSpeechSynthesisVoiceQuality) -> String {
        switch quality {
        case .default: return "Padrão"
        case .enhanced: return "Melhorada"
        case .premium: return "Premium"
        @unknown default: return "Padrão"
        }
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didStart utterance: AVSpeechUtterance) {
        Task { @MainActor in delegate?.speechDidStart() }
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        Task { @MainActor in delegate?.speechDidFinish() }
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        Task { @MainActor in delegate?.speechDidFinish() }
    }
}
