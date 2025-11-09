package com.example.auctionserver.model.dto

data class OllamaEmbeddingResponse(
    val model: String? = null,
    val embedding: List<Double>? = null,
    val embeddings: List<DoubleArray>? = null
)