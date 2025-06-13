package com.example.auctionserver.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "lots")
data class Lot(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val startPrice: BigDecimal,
    var currentPrice: BigDecimal,
    var status: String = "OPEN",

    @ManyToOne
    @JoinColumn(name = "owner_id")
    val owner: User,

    var endTime: Instant? = null
)