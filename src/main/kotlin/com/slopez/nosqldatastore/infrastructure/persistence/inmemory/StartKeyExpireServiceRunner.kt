package com.slopez.nosqldatastore.infrastructure.persistence.inmemory

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component


@Component
class StartKeyExpireServiceRunner(@Autowired val nosqlDataStoreExpireKeyService: KotlinNosqlDataStoreExpireKeyService) : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        runBlocking { nosqlDataStoreExpireKeyService.start() }
    }
}