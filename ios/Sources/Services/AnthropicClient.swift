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

struct AnthropicResult {
    let text: String
    let requestedModel: String
    let responseModel: String
    let inputTokens: Int?
    let outputTokens: Int?
}

struct AnthropicResponse: Codable {
    struct Content: Codable {
        let type: String?
        let text: String?
    }
    struct Usage: Codable {
        let input_tokens: Int?
        let output_tokens: Int?
    }
    let model: String?
    let content: [Content]
    let usage: Usage?
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
        try await sendDetailed(apiKey: apiKey, model: model, system: system, messages: messages, maxTokens: 400).text
    }

    func testModel(apiKey: String, model: String) async throws -> AnthropicResult {
        try await sendDetailed(
            apiKey: apiKey,
            model: model,
            system: "Responda apenas: teste ok.",
            messages: [.init(role: "user", content: "Teste de conectividade. Qual modelo recebeu esta requisição?")],
            maxTokens: 80
        )
    }

    private func sendDetailed(apiKey: String, model: String, system: String, messages: [ClaudeMessage], maxTokens: Int) async throws -> AnthropicResult {
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
            max_tokens: maxTokens,
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
        let text = decoded.content.first?.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "Recebi uma resposta vazia, Senhor."
        return AnthropicResult(
            text: text,
            requestedModel: model,
            responseModel: decoded.model ?? model,
            inputTokens: decoded.usage?.input_tokens,
            outputTokens: decoded.usage?.output_tokens
        )
    }
}
