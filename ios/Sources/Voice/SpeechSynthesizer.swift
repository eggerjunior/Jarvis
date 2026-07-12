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

    private override init() {
        super.init()
        synthesizer.delegate = self
    }

    func speak(_ text: String) {
        synthesizer.stopSpeaking(at: .immediate)
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = selectedVoice()
        utterance.rate = 0.48
        utterance.pitchMultiplier = 0.72
        synthesizer.speak(utterance)
    }

    func stop() {
        synthesizer.stopSpeaking(at: .immediate)
    }

    private func selectedVoice() -> AVSpeechSynthesisVoice? {
        let voices = AVSpeechSynthesisVoice.speechVoices()

        switch voicePreference {
        case .automatic:
            return AVSpeechSynthesisVoice(language: "pt-BR")
        case .masculine:
            return preferredVoice(in: voices, gender: .male)
                ?? AVSpeechSynthesisVoice(language: "pt-BR")
        case .feminine:
            return preferredVoice(in: voices, gender: .female)
                ?? AVSpeechSynthesisVoice(language: "pt-BR")
        }
    }

    private func preferredVoice(in voices: [AVSpeechSynthesisVoice], gender: AVSpeechSynthesisVoiceGender) -> AVSpeechSynthesisVoice? {
        voices.first { $0.language == "pt-BR" && $0.gender == gender }
            ?? voices.first { $0.language.hasPrefix("pt-") && $0.gender == gender }
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
