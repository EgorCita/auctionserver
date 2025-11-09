package com.example.auctionserver.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
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
    val imageUrl: String,
    val description: String? = null,
    val startPrice: BigDecimal,
    var currentPrice: BigDecimal,
    var status: String = "OPEN",

    @ManyToOne
    @JoinColumn(name = "owner_id")
    val owner: User,

    @ManyToOne
    @JoinColumn(name = "winner_id")
    var winner: User? = null,

    var endTime: Instant? = null,

    @Transient
    @JsonIgnore
    @Column(columnDefinition = "vector(768)")
    val descriptionEmbedding: DoubleArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lot

        if (id != other.id) return false
        if (title != other.title) return false
        if (imageUrl != other.imageUrl) return false
        if (description != other.description) return false
        if (startPrice != other.startPrice) return false
        if (currentPrice != other.currentPrice) return false
        if (status != other.status) return false
        if (owner != other.owner) return false
        if (winner != other.winner) return false
        if (endTime != other.endTime) return false
        if (!descriptionEmbedding.contentEquals(other.descriptionEmbedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + imageUrl.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + startPrice.hashCode()
        result = 31 * result + currentPrice.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + (winner?.hashCode() ?: 0)
        result = 31 * result + (endTime?.hashCode() ?: 0)
        result = 31 * result + (descriptionEmbedding?.contentHashCode() ?: 0)
        return result
    }
}