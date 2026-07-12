import AVFoundation
import Foundation

@MainActor
protocol JarvisSpeechSynthesizerDelegate: AnyObject {
    func speechDidStart()
    func speechDidFinish()
}

final class JarvisSpeechSynthesizer: NSObject, AVSpeechSynthesizerDelegate, @unchecked Sendable {
    static let shared = JarvisSpeechSynthesizer()

    private let synthesizer = AVSpeechSynthesizer()
    weak var delegate: JarvisSpeechSynthesizerDelegate?

    private override init() {
        super.init()
        synthesizer.delegate = self
    }

    func speak(_ text: String) {
        synthesizer.stopSpeaking(at: .immediate)
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: "pt-BR")
        utterance.rate = 0.48
        utterance.pitchMultiplier = 0.72
        synthesizer.speak(utterance)
    }

    func stop() {
        synthesizer.stopSpeaking(at: .immediate)
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
