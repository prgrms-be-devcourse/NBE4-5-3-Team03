package com.example.Flicktionary.global.exception

class ServiceException(val code: Int, message: String) : RuntimeException(message)
