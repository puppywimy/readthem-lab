package com.back

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
class BackApplication

fun main(args: Array<String>) {
    runApplication<BackApplication>(*args)
}