package com.example.auctionserver.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController {
    @GetMapping
    fun test(): String {
        println("Тестовый эндпоинт работает!")
        return "OK"
    }
}