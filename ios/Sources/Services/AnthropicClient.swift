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

struct AIModelResult {
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

struct OpenRouterRequest: Codable {
    let model: String
    let messages: [ClaudeMessage]
    let max_tokens: Int
}

struct OpenRouterResponse: Codable {
    struct Choice: Codable {
        let message: ClaudeMessage?
    }
    struct Usage: Codable {
        let prompt_tokens: Int?
        let completion_tokens: Int?
    }
    let model: String?
    let choices: [Choice]
    let usage: Usage?
}

struct OpenRouterErrorResponse: Codable {
    struct APIError: Codable {
        let message: String?
        let code: Int?
    }
    let error: APIError?
}

final class AIModelClient {
    func send(provider: AIProvider, apiKey: String, model: String, system: String, messages: [ClaudeMessage]) async throws -> String {
        try await sendDetailed(provider: provider, apiKey: apiKey, model: model, system: system, messages: messages, maxTokens: 400).text
    }

    func testModel(provider: AIProvider, apiKey: String, model: String) async throws -> AIModelResult {
        try await sendDetailed(
            provider: provider,
            apiKey: apiKey,
            model: model,
            system: "Responda apenas: teste ok.",
            messages: [.init(role: "user", content: "Teste de conectividade. Qual modelo recebeu esta requisição?")],
            maxTokens: 80
        )
    }

    private func sendDetailed(provider: AIProvider, apiKey: String, model: String, system: String, messages: [ClaudeMessage], maxTokens: Int) async throws -> AIModelResult {
        switch provider {
        case .anthropic:
            return try await sendAnthropic(apiKey: apiKey, model: model, system: system, messages: messages, maxTokens: maxTokens)
        case .openRouter:
            return try await sendOpenRouter(apiKey: apiKey, model: model, system: system, messages: messages, maxTokens: maxTokens)
        }
    }

    private func sendAnthropic(apiKey: String, model: String, system: String, messages: [ClaudeMessage], maxTokens: Int) async throws -> AIModelResult {
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
        return AIModelResult(
            text: text,
            requestedModel: model,
            responseModel: decoded.model ?? model,
            inputTokens: decoded.usage?.input_tokens,
            outputTokens: decoded.usage?.output_tokens
        )
    }

    private func sendOpenRouter(apiKey: String, model: String, system: String, messages: [ClaudeMessage], maxTokens: Int) async throws -> AIModelResult {
        guard let url = URL(string: "https://openrouter.ai/api/v1/chat/completions") else {
            throw NSError(domain: "Jarvis", code: -1, userInfo: [NSLocalizedDescriptionKey: "URL do OpenRouter inválida."])
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Jarvis", forHTTPHeaderField: "X-OpenRouter-Title")

        var routedMessages = [ClaudeMessage(role: "system", content: system)]
        routedMessages.append(contentsOf: messages)
        request.httpBody = try JSONEncoder().encode(OpenRouterRequest(
            model: model,
            messages: routedMessages,
            max_tokens: maxTokens
        ))

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw NSError(domain: "Jarvis", code: -2, userInfo: [NSLocalizedDescriptionKey: "Resposta inválida do OpenRouter."])
        }

        guard (200...299).contains(http.statusCode) else {
            let decoded = try? JSONDecoder().decode(OpenRouterErrorResponse.self, from: data)
            let message = decoded?.error?.message ?? "Erro HTTP \(http.statusCode)."
            throw NSError(domain: "Jarvis", code: http.statusCode, userInfo: [NSLocalizedDescriptionKey: message])
        }

        let decoded = try JSONDecoder().decode(OpenRouterResponse.self, from: data)
        let text = decoded.choices.first?.message?.content.trimmingCharacters(in: .whitespacesAndNewlines) ?? "Recebi uma resposta vazia, Senhor."
        return AIModelResult(
            text: text,
            requestedModel: model,
            responseModel: decoded.model ?? model,
            inputTokens: decoded.usage?.prompt_tokens,
            outputTokens: decoded.usage?.completion_tokens
        )
    }
}
