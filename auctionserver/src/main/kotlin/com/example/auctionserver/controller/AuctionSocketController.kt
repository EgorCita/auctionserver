package com.example.auctionserver.controller

import com.example.auctionserver.model.dto.BidMessage
import com.example.auctionserver.model.entity.Lot
import com.example.auctionserver.repository.UserRepository
import com.example.auctionserver.service.AuctionService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import java.math.BigDecimal
import java.security.Principal

@Controller
class AuctionSocketController(
    private val auctionService: AuctionService,
    private val userRepository: UserRepository
) {

    @MessageMapping("/lot/{lotId}/bid")
    @SendTo("/topic/lot/{lotId}")
    fun handleBid(
        @DestinationVariable lotId: Long,
        @Payload bid: BidMessage,
        @Header("simpUser") user: Principal
    ): Lot {
        val lot = auctionService.placeBid(lotId, bid.amount, userRepository.findByUsername(user.name)!!)
        return lot
    }
}