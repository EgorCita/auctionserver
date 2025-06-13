package com.example.auctionserver.controller

import com.example.auctionserver.model.dto.AuthRequest
import com.example.auctionserver.model.dto.AuthResponse
import com.example.auctionserver.model.entity.User
import com.example.auctionserver.repository.UserRepository
import com.example.auctionserver.security.JwtTokenProvider
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/register")
    fun register(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        println("Enter registration")
        if (userRepository.findByUsername(request.username) != null) {
            throw BadCredentialsException("Username already exists") as Throwable
        }

        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password)
        )
        userRepository.save(user)

        val token = jwtTokenProvider.generateToken(user.username, user.role)
        return ResponseEntity.ok(AuthResponse(token))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        println("Enter login")
        val user = userRepository.findByUsername(request.username) ?: throw BadCredentialsException("User not found")
        println("User founded")

        if (!passwordEncoder.matches(request.password, user.password)) {
            println("Invalid password")
            throw BadCredentialsException("Invalid password")
        }

        val token = jwtTokenProvider.generateToken(user.username, user.role)
        return ResponseEntity.ok(AuthResponse(token))
    }
}