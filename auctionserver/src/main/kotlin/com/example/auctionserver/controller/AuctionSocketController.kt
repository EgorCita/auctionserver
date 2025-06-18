package com.example.auctionserver.controller

import com.example.auctionserver.model.dto.BidMessage
import com.example.auctionserver.model.entity.Lot
import com.example.auctionserver.model.entity.User
import com.example.auctionserver.repository.UserRepository
import com.example.auctionserver.service.AuctionService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
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
        principal: Principal
    ): Lot {
        println("Entering handleBid")
        println("principal = $principal")

        val lot = auctionService.placeBid(lotId, bid.amount, userRepository.findByUsername(principal.name)!!)
        return lot
    }

}