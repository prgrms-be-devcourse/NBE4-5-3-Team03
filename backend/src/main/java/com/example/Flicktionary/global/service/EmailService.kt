package com.example.Flicktionary.global.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val javaMailSender: JavaMailSender
) {
    @Value("\${custom.mail.sender}")
    private lateinit var emailSenderAddress: String

    fun sendSimpleEmail(to: String, subject: String, content: String) {
        val message = SimpleMailMessage()
        message.from = emailSenderAddress
        message.setTo(to)
        message.subject = subject
        message.text = content
        javaMailSender.send(message)
    }
}