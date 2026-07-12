import ActivityKit
import Foundation

struct JarvisLiveActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var status: String
        var prompt: String
        var response: String
        var updatedAt: Date

        static let preview = ContentState(
            status: "Ouvindo",
            prompt: "Ei Jarvis, resumo do caminho.",
            response: "Rota e lembretes prontos para consulta por voz.",
            updatedAt: Date()
        )
    }

    var sessionName: String

    static let preview = JarvisLiveActivityAttributes(sessionName: "Jarvis")
}
