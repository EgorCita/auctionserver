package com.example.auctionserver.controller

import com.example.auctionserver.model.dto.CreateLotDto
import com.example.auctionserver.model.entity.Lot
import com.example.auctionserver.model.entity.User
import com.example.auctionserver.repository.LotRepository
import com.example.auctionserver.service.AuctionService
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/lots")
class LotController(
    private val auctionService: AuctionService,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    @GetMapping("/")
    fun lots(): ResponseEntity<List<Lot>> {
        println("Calling list of lots")
        return ResponseEntity.ok(auctionService.getAllLots())
    }

    @PostMapping("/")
    fun createLot(@RequestBody dto: CreateLotDto, user: User): ResponseEntity<Lot> {
        println("Calling creating of lots")
        val lot = auctionService.createLot(dto, user)
        notifyLotUpdate(lot)
        return ResponseEntity.ok(lot)
    }

    @PostMapping("/{lotId}/bid")
    fun placeBid(
        @PathVariable lotId: Long,
        @RequestBody amount: BigDecimal,
        user: User
    ): ResponseEntity<Lot> {
        val lot = auctionService.placeBid(lotId, amount, user)
        notifyLotUpdate(lot)
        return ResponseEntity.ok(lot)
    }

    @PostMapping("/{lotId}/close")
    fun closeLot(@PathVariable lotId: Long, user: User): ResponseEntity<Lot> {
        val lot = auctionService.closeLot(lotId, user)
        notifyLotUpdate(lot)
        return ResponseEntity.ok(lot)
    }

    @PostMapping("/{lotId}/finalize")
    fun finalizeLot(@PathVariable lotId: Long, user: User): ResponseEntity<Lot> {
        val lot = auctionService.finalizeLot(lotId, user)
        notifyLotUpdate(lot)
        return ResponseEntity.ok(lot)
    }

    private fun notifyLotUpdate(lot: Lot) {
        simpMessagingTemplate.convertAndSend("/topic/lot/${lot.id}", lot)
    }
}