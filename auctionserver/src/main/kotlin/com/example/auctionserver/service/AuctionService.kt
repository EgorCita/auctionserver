package com.example.auctionserver.service

import com.example.auctionserver.model.dto.CreateLotDto
import com.example.auctionserver.model.entity.Bid
import com.example.auctionserver.model.entity.Lot
import com.example.auctionserver.model.entity.User
import com.example.auctionserver.repository.BidRepository
import com.example.auctionserver.repository.LotRepository
import com.example.auctionserver.repository.UserRepository
import org.hibernate.type.NullType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class AuctionService(
    private val bidRepository: BidRepository,
    private val lotRepository: LotRepository,
    private val userRepository: UserRepository
) {
    fun createLot(lotDto: CreateLotDto, owner: User): Lot {
        val lot = Lot(
            title = lotDto.title,
            description = lotDto.description,
            startPrice = lotDto.startPrice,
            currentPrice = lotDto.startPrice,
            owner = owner,
            status = "OPEN"
        )
        return lotRepository.save(lot)
    }

    fun placeBid(lotId: Long, amount: BigDecimal, bidder: User): Lot {
        val lot = lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") }

        if (lot.status != "OPEN") throw IllegalStateException("Lot is not open for bidding")
        if (amount <= lot.currentPrice) throw IllegalArgumentException("Bid must be higher than current price")

        val bid = Bid(
            amount = amount,
            bidder = bidder,
            lot = lot,
            timestamp = Instant.now()
        )
        bidRepository.save(bid)

        lot.currentPrice = amount
        lot.winner = bidder
        return lotRepository.save(lot)
    }

    fun finalizeLot(lotId: Long, owner: User): Lot {
        val lot = lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") }

        if (lot.owner.id != owner.id) throw IllegalStateException("Only owner can finalize the lot")
        if (lot.status != "OPEN") throw IllegalStateException("Lot must be open to finalize")

        lot.status = "CLOSING"
        lot.endTime = Instant.now().plusSeconds(60) // 60 seconds to close
        return lotRepository.save(lot)
    }

    fun closeLot(lotId: Long, owner: User): Lot {
        val lot = lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") }

        if (lot.owner.id != owner.id) throw IllegalStateException("Only owner can close the lot")
        if (lot.status != "CLOSING") throw IllegalStateException("Lot is not in finalizing state")

        lot.status = "SOLD"
        return lotRepository.save(lot)
    }

    fun getAllLots() : List<Lot> {
        return lotRepository.findAll()
    }

    fun getAllBidsInLot(lotId: Long) : List<Bid> {
        return bidRepository.findByLot(lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") })
    }
}