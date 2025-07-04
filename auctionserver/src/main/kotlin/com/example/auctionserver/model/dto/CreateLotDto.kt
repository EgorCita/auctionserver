package com.example.auctionserver.model.dto

import java.math.BigDecimal

data class CreateLotDto(val title: String, val imageUrl: String, val description: String?, val startPrice: BigDecimal)