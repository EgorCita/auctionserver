package com.example.auctionserver.repository

import com.example.auctionserver.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String?): User?
}