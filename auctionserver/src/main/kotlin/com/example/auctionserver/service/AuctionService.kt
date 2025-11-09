package com.example.auctionserver.service

import com.example.auctionserver.model.dto.CreateLotDto
import com.example.auctionserver.model.entity.Bid
import com.example.auctionserver.model.entity.Lot
import com.example.auctionserver.model.entity.User
import com.example.auctionserver.repository.BidRepository
import com.example.auctionserver.repository.LotRepository
import com.example.auctionserver.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class AuctionService(
    private val bidRepository: BidRepository,
    private val lotRepository: LotRepository,
    private val userRepository: UserRepository,
    private val lotClosingScheduler: LotClosingScheduler,
    private val ollamaEmbeddingService: OllamaEmbeddingService,
    private val jdbcTemplate: JdbcTemplate
) {
    @Transactional
    fun createLot(lotDto: CreateLotDto, owner: User): Lot {
        val lot = Lot(
            title = lotDto.title,
            imageUrl = lotDto.imageUrl,
            description = lotDto.description,
            startPrice = lotDto.startPrice,
            currentPrice = lotDto.startPrice,
            owner = owner,
            status = "OPEN"
        )

        val savedLot = lotRepository.save(lot)

        val embedding = ollamaEmbeddingService.generateEmbedding(lotDto.description)
        val embeddingString = embedding.joinToString(", ", "[", "]")

        val updateSql = """
            UPDATE lots 
            SET description_embedding = ?::vector 
            WHERE id = ?
        """

        jdbcTemplate.update(updateSql, embeddingString, savedLot.id)

        return savedLot
    }

    fun placeBid(lotId: Long, amount: BigDecimal, bidder: User): Lot {
        val lot = lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") }

        if (lot.status == "SOLD") throw IllegalStateException("Lot is not open for bidding")
        if (amount <= lot.currentPrice) throw IllegalArgumentException("Bid must be higher than current price")

        val bid = Bid(
            amount = amount,
            bidder = bidder,
            lot = lot,
            timestamp = Instant.now()
        )
        bidRepository.save(bid)

        if (lot.status == "CLOSING") {
            lotClosingScheduler.cancelScheduledClosing(lotId)
            lot.status = "OPEN"
            lot.endTime = null
        }

        lot.currentPrice = amount
        lot.winner = bidder
        return lotRepository.save(lot)
    }

    fun finalizeLot(lotId: Long, owner: User): Lot {
        val lot = lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") }

        if (lot.owner.id != owner.id) throw IllegalStateException("Only owner can finalize the lot")
        if (lot.status != "OPEN") throw IllegalStateException("Lot must be open to finalize")

        val endTime = Instant.now().plusSeconds(60) // 60 seconds to close
        lotClosingScheduler.scheduleLotClosing(lotId, owner, endTime)

        lot.status = "CLOSING"
        lot.endTime = endTime
        return lotRepository.save(lot)
    }

    fun closeLot(lotId: Long, owner: User): Lot {
        val lot = lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") }

        if (lot.owner.id != owner.id) throw IllegalStateException("Only owner can close the lot")
        if (lot.status != "CLOSING") throw IllegalStateException("Lot is not in finalizing state")

        lotClosingScheduler.cancelScheduledClosing(lotId)

        lot.status = "SOLD"
        lot.endTime = Instant.now()

        return lotRepository.save(lot)
    }

    fun getAllLots() : List<Lot> {
        return lotRepository.findAll()
    }

    fun getAllLotsWhereUserWon(user: User) : List<Lot> {
        return lotRepository.findByStatusAndWinner("SOLD", user)
    }

fun getSimilarLots(userQuery: String, limit: Int = 3): List<Lot> {
    return try {
        val queryEmbedding = ollamaEmbeddingService.generateEmbedding(userQuery)
        val embeddingString = queryEmbedding.joinToString(", ", "[", "]")

        val sql = """
                SELECT l.*, 
                       1 - (l.description_embedding <=> ?::vector) as similarity
                FROM lots l
                WHERE l.description_embedding IS NOT NULL
                ORDER BY l.description_embedding <=> ?::vector
                LIMIT ?
            """

        val results = jdbcTemplate.query(sql, { rs, _ ->
            Lot(
                id = rs.getLong("id"),
                title = rs.getString("title"),
                imageUrl = rs.getString("image_url"),
                description = rs.getString("description"),
                startPrice = rs.getBigDecimal("start_price"),
                currentPrice = rs.getBigDecimal("current_price"),
                status = rs.getString("status"),
                owner = userRepository.findById(rs.getLong("owner_id"))
                    .orElseThrow { RuntimeException("User not found with id: ${rs.getLong("owner_id")}") },
                winner = if (rs.getObject("winner_id") != null) {
                    userRepository.findById(rs.getLong("winner_id"))
                        .orElse(null)
                } else null,
                endTime = if (rs.getTimestamp("end_time") != null) {
                    rs.getTimestamp("end_time").toInstant()
                } else null,
                descriptionEmbedding = null
            )
        }, embeddingString, embeddingString, limit)

        println("Found ${results.size} similar lots for query: '$userQuery'")
        results

    } catch (e: Exception) {
        println("Error searching similar lots for query: $userQuery, ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}

    fun getAllBidsInLot(lotId: Long) : List<Bid> {
        return bidRepository.findByLot(lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") })
    }
}