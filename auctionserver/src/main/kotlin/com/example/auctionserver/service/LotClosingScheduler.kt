package com.example.auctionserver.service

import com.example.auctionserver.model.entity.User
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
    private val auctionService: AuctionService,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    private val scheduledTasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()

    fun cancelScheduledClosing(lotId: Long) {
        scheduledTasks[lotId]?.cancel(true)
        scheduledTasks.remove(lotId)
    }

    fun scheduleLotClosing(lotId: Long, user: User, endTime: Instant) {
        // Отменяем предыдущую задачу, если была
        scheduledTasks[lotId]?.cancel(true)

        val task = Runnable {
            try {
                val lot = auctionService.closeLot(lotId, user)
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