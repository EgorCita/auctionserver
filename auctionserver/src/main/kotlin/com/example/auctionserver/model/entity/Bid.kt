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
@Table(name = "bids")
data class Bid(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val amount: BigDecimal,

    @ManyToOne
    @JoinColumn(name = "bidder_id")
    val bidder: User,

    @ManyToOne
    @JoinColumn(name = "lot_id")
    val lot: Lot,

    val timestamp: Instant = Instant.now()
)