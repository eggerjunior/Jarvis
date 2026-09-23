package br.app.egger.jarvis.service

import br.app.egger.jarvis.model.AIProvider
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ClaudeMessage(
    val role: String,
    val content: String
)

data class AIModelResult(
    val text: String,
    val requestedModel: String,
    val responseModel: String,
    val inputTokens: Int?,
    val outputTokens: Int?
)

// Anthropic Request & Response DTOs
private data class AnthropicRequest(
    val model: String,
    @SerializedName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ClaudeMessage>
)

private data class AnthropicResponse(
    val model: String?,
    val content: List<Content>?,
    val usage: Usage?
) {
    data class Content(val type: String?, val text: String?)
    data class Usage(
        @SerializedName("input_tokens") val inputTokens: Int?,
        @SerializedName("output_tokens") val outputTokens: Int?
    )
}

private data class AnthropicErrorResponse(
    val error: APIError?
) {
    data class APIError(val type: String?, val message: String?)
}

// OpenRouter Request & Response DTOs
private data class OpenRouterTool(
    val type: String
)

private data class OpenRouterRequest(
    val model: String,
    val messages: List<ClaudeMessage>,
    @SerializedName("max_tokens") val maxTokens: Int,
    val tools: List<OpenRouterTool>?
)

private data class OpenRouterResponse(
    val model: String?,
    val choices: List<Choice>?,
    val usage: Usage?
) {
    data class Choice(val message: ClaudeMessage?)
    data class Usage(
        @SerializedName("prompt_tokens") val promptTokens: Int?,
        @SerializedName("completion_tokens") val completionTokens: Int?
    )
}

private data class OpenRouterErrorResponse(
    val error: APIError?
) {
    data class APIError(val message: String?, val code: Int?)
}

class AIModelClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun send(
        provider: AIProvider,
        apiKey: String,
        model: String,
        system: String,
        messages: List<ClaudeMessage>,
        enableWebSearch: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        sendDetailed(
            provider = provider,
            apiKey = apiKey,
            model = model,
            system = system,
            messages = messages,
            maxTokens = 400,
            enableWebSearch = enableWebSearch
        ).text
    }

    suspend fun testModel(
        provider: AIProvider,
        apiKey: String,
        model: String
    ): AIModelResult = withContext(Dispatchers.IO) {
        sendDetailed(
            provider = provider,
            apiKey = apiKey,
            model = model,
            system = "Responda apenas: teste ok.",
            messages = listOf(ClaudeMessage("user", "Teste de conectividade. Qual modelo recebeu esta requisição?")),
            maxTokens = 80,
            enableWebSearch = false
        )
    }

    private fun sendDetailed(
        provider: AIProvider,
        apiKey: String,
        model: String,
        system: String,
        messages: List<ClaudeMessage>,
        maxTokens: Int,
        enableWebSearch: Boolean
    ): AIModelResult {
        return when (provider) {
            AIProvider.ANTHROPIC -> sendAnthropic(apiKey, model, system, messages, maxTokens)
            AIProvider.OPEN_ROUTER -> sendOpenRouter(apiKey, model, system, messages, maxTokens, enableWebSearch)
        }
    }

    private fun sendAnthropic(
        apiKey: String,
        model: String,
        system: String,
        messages: List<ClaudeMessage>,
        maxTokens: Int
    ): AIModelResult {
        val payload = AnthropicRequest(
            model = model,
            maxTokens = maxTokens,
            system = system,
            messages = messages
        )
        val requestBody = gson.toJson(payload).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .post(requestBody)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val err = gson.fromJson(responseBody, AnthropicErrorResponse::class.java)
                err?.error?.message ?: "Erro HTTP ${response.code}."
            } catch (e: Exception) {
                "Erro HTTP ${response.code}."
            }
            throw IOException(errorMsg)
        }

        val decoded = gson.fromJson(responseBody, AnthropicResponse::class.java)
        val text = decoded?.content?.firstOrNull()?.text?.trim() ?: "Recebi uma resposta vazia, Senhor."

        return AIModelResult(
            text = text,
            requestedModel = model,
            responseModel = decoded?.model ?: model,
            inputTokens = decoded?.usage?.inputTokens,
            outputTokens = decoded?.usage?.outputTokens
        )
    }

    private fun sendOpenRouter(
        apiKey: String,
        model: String,
        system: String,
        messages: List<ClaudeMessage>,
        maxTokens: Int,
        enableWebSearch: Boolean
    ): AIModelResult {
        val routedMessages = mutableListOf(ClaudeMessage("system", system))
        routedMessages.addAll(messages)

        val tools = if (enableWebSearch) listOf(OpenRouterTool("openrouter:web_search")) else null

        val payload = OpenRouterRequest(
            model = model,
            messages = routedMessages,
            maxTokens = maxTokens,
            tools = tools
        )
        val requestBody = gson.toJson(payload).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-OpenRouter-Title", "Jarvis")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val err = gson.fromJson(responseBody, OpenRouterErrorResponse::class.java)
                err?.error?.message ?: "Erro HTTP ${response.code}."
            } catch (e: Exception) {
                "Erro HTTP ${response.code}."
            }
            throw IOException(errorMsg)
        }

        val decoded = gson.fromJson(responseBody, OpenRouterResponse::class.java)
        val text = decoded?.choices?.firstOrNull()?.message?.content?.trim() ?: "Recebi uma resposta vazia, Senhor."

        return AIModelResult(
            text = text,
            requestedModel = model,
            responseModel = decoded?.model ?: model,
            inputTokens = decoded?.usage?.promptTokens,
            outputTokens = decoded?.usage?.completionTokens
        )
    }
}
