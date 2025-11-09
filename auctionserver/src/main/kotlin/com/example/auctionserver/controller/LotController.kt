package com.example.auctionserver.controller

import com.example.auctionserver.model.dto.CreateLotDto
import com.example.auctionserver.model.entity.Bid
import com.example.auctionserver.model.entity.Lot
import com.example.auctionserver.model.entity.User
import com.example.auctionserver.service.AuctionService
import com.example.auctionserver.service.LotClosingScheduler
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/lots")
class LotController(
    private val auctionService: AuctionService,
    private val lotClosingScheduler: LotClosingScheduler,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    @GetMapping("/")
    fun lots(): ResponseEntity<List<Lot>> {
        println("Calling list of lots")
        return ResponseEntity.ok(auctionService.getAllLots())
    }

    @GetMapping("/search")
    fun searchLots(@RequestParam query: String): ResponseEntity<List<Lot>> {
        println("Calling searching suitable lots")
        return ResponseEntity.ok(auctionService.getSimilarLots(query))
    }

    @GetMapping("/me")
    fun lotsWon(
        user: User
    ): ResponseEntity<List<Lot>> {
        println("getAllLotsWhereUserWon; user = $user")
        println(auctionService.getAllLotsWhereUserWon(user))
        return ResponseEntity.ok(auctionService.getAllLotsWhereUserWon(user))
    }

    @PostMapping("/")
    fun createLot(@RequestBody dto: CreateLotDto, user: User): ResponseEntity<Lot> {
        println("Calling creating of lots")
        val lot = auctionService.createLot(dto, user)
        notifyLotUpdate(lot)
        return ResponseEntity.ok(lot)
    }

    @GetMapping("/{lotId}/bids")
    fun bids(
        @PathVariable lotId: Long,
    ): ResponseEntity<List<Bid>> {
        println("getAllBidsInLot; lotId = $lotId")
        println(auctionService.getAllBidsInLot(lotId))
        return ResponseEntity.ok(auctionService.getAllBidsInLot(lotId))
    }

    @PostMapping("/{lotId}/finalize")
    fun finalizeLot(@PathVariable lotId: Long, user: User): ResponseEntity<Lot> {
        val lot = auctionService.finalizeLot(lotId, user)
        notifyLotUpdate(lot)
        return ResponseEntity.ok(lot)
    }

    @PostMapping("/{lotId}/close")
    fun closeLot(@PathVariable lotId: Long, user: User): ResponseEntity<Lot> {
        val lot = auctionService.closeLot(lotId, user)
        notifyLotUpdate(lot)
        return ResponseEntity.ok(lot)
    }

    private fun notifyLotUpdate(lot: Lot) {
        simpMessagingTemplate.convertAndSend("/topic/lot/${lot.id}", lot)
    }
}