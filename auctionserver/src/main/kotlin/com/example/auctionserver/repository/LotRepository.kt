package com.example.auctionserver.repository

import com.example.auctionserver.model.entity.Lot
import com.example.auctionserver.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface LotRepository : JpaRepository<Lot, Long> {
    fun findByStatusAndWinner(status: String, winner: User): List<Lot>
}