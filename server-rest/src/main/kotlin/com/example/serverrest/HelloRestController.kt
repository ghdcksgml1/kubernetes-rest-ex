package com.example.serverrest

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
class HelloRestController(
    @Value("\${rest.url}") val url: String

) {

    @GetMapping("/")
    fun getRest(): String {
        val restTemplate = RestTemplate()

        return restTemplate?.getForObject(url, String::class.java) ?: "error"
    }
}