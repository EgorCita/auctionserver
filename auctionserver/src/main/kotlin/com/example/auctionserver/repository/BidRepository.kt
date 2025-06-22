package com.example.auctionserver.repository

import com.example.auctionserver.model.entity.Bid
import com.example.auctionserver.model.entity.Lot
import org.springframework.data.jpa.repository.JpaRepository

interface BidRepository : JpaRepository<Bid, Long> {
    fun findByLot(lot: Lot): List<Bid>
}