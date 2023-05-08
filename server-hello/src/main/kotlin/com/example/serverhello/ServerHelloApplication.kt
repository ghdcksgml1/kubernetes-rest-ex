package com.example.serverhello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServerHelloApplication

fun main(args: Array<String>) {
	runApplication<ServerHelloApplication>(*args)
}
