package com.example.auctionserver.service

// Файл: OllamaEmbeddingService.kt
import com.example.auctionserver.model.dto.OllamaEmbeddingRequest
import com.example.auctionserver.model.dto.OllamaEmbeddingResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class OllamaEmbeddingService {

    private val ollamaBaseUrl = "http://localhost:11434"
    private val embeddingModel = "nomic-embed-text"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
        expectSuccess = false
    }

    fun generateEmbedding(text: String?): DoubleArray {
        return runBlocking {
            try {
                require(text != null && text.isNotEmpty()) { "The sent text should not be null or empty." }

                val request = OllamaEmbeddingRequest(model = embeddingModel, prompt = text)

                val response = client.post("$ollamaBaseUrl/api/embeddings") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                // Проверяем статус ответа
                if (!response.status.isSuccess()) {
                    val errorBody = response.body<String>()
                    throw RuntimeException("Ollama API error: ${response.status}, body: $errorBody")
                }

                val responseBody = response.body<OllamaEmbeddingResponse>()

                val embedding = when {
                    responseBody.embedding != null -> {
                        responseBody.embedding.toDoubleArray()
                    }
                    responseBody.embeddings != null && responseBody.embeddings.isNotEmpty() -> {
                        responseBody.embeddings.first()
                    }
                    else -> throw IllegalArgumentException("Incorrect response format from Ollama: $responseBody")
                }

                println("Dimension embedding has been successfully generated: ${embedding.size}")
                embedding

            } catch (e: Exception) {
                throw RuntimeException("Error when generating embedding for text: '$text'. " +
                        "Make sure that the model '$embeddingModel' downloaded and launched. Mistake: ${e.message}", e)
            }
        }
    }
}