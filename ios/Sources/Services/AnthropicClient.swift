import Foundation

struct ClaudeMessage: Codable {
    let role: String
    let content: String
}

struct AnthropicRequest: Codable {
    let model: String
    let max_tokens: Int
    let system: String
    let messages: [ClaudeMessage]
}

struct AnthropicResponse: Codable {
    struct Content: Codable {
        let type: String?
        let text: String?
    }
    let content: [Content]
}

struct AnthropicErrorResponse: Codable {
    struct APIError: Codable {
        let type: String?
        let message: String?
    }
    let error: APIError?
}

final class AnthropicClient {
    func send(apiKey: String, model: String, system: String, messages: [ClaudeMessage]) async throws -> String {
        guard let url = URL(string: "https://api.anthropic.com/v1/messages") else {
            throw NSError(domain: "Jarvis", code: -1, userInfo: [NSLocalizedDescriptionKey: "URL da Anthropic inválida."])
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(apiKey, forHTTPHeaderField: "x-api-key")
        request.setValue("2023-06-01", forHTTPHeaderField: "anthropic-version")
        request.setValue("application/json", forHTTPHeaderField: "content-type")
        request.httpBody = try JSONEncoder().encode(AnthropicRequest(
            model: model,
            max_tokens: 400,
            system: system,
            messages: messages
        ))

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw NSError(domain: "Jarvis", code: -2, userInfo: [NSLocalizedDescriptionKey: "Resposta inválida da Anthropic."])
        }

        guard (200...299).contains(http.statusCode) else {
            let decoded = try? JSONDecoder().decode(AnthropicErrorResponse.self, from: data)
            let message = decoded?.error?.message ?? "Erro HTTP \(http.statusCode)."
            throw NSError(domain: "Jarvis", code: http.statusCode, userInfo: [NSLocalizedDescriptionKey: message])
        }

        let decoded = try JSONDecoder().decode(AnthropicResponse.self, from: data)
        return decoded.content.first?.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "Recebi uma resposta vazia, Senhor."
    }
}
