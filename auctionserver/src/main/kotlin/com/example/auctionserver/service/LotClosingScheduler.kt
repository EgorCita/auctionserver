package com.example.auctionserver.service

import com.example.auctionserver.model.entity.User
import com.example.auctionserver.repository.LotRepository
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Service
class LotClosingScheduler(
    private val taskScheduler: TaskScheduler,
    private val lotRepository: LotRepository,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    private val scheduledTasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()

    fun cancelScheduledClosing(lotId: Long) {
        scheduledTasks[lotId]?.cancel(true)
        scheduledTasks.remove(lotId)
    }

    fun scheduleLotClosing(lotId: Long, user: User, endTime: Instant) {
        scheduledTasks[lotId]?.cancel(true)

        val task = Runnable {
            try {
                val lot = lotRepository.findById(lotId).orElseThrow { NoSuchElementException("Lot not found") }

                if (lot.owner.id != user.id) throw IllegalStateException("Only owner can close the lot")
                if (lot.status != "CLOSING") throw IllegalStateException("Lot is not in finalizing state")

                lot.status = "SOLD"
                lot.endTime = Instant.now()

                lotRepository.save(lot)

                simpMessagingTemplate.convertAndSend("/topic/lot/$lotId", lot)
                scheduledTasks.remove(lotId)
            } catch (ex: Exception) {
                logger.error("Failed to automatically close lot $lotId", ex)
            }
        }

        scheduledTasks[lotId] = taskScheduler.schedule(
            task,
            endTime
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LotClosingScheduler::class.java)
    }
}