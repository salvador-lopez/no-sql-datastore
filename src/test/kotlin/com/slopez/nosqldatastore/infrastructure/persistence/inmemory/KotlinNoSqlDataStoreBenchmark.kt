package com.slopez.nosqldatastore.infrastructure.persistence.inmemory

import kotlinx.coroutines.*
import org.junit.jupiter.api.assertDoesNotThrow
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Warmup(iterations = 0)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(-1)
internal class KotlinNoSqlDataStoreBenchmark {
    private lateinit var key: String
    private lateinit var value: String
    private lateinit var dataStore: KotlinNoSqlDataStore

    @Setup
    internal fun setUp() {
        key = "bench-key"
        value = "bench-value"

        val expireKeyService = KotlinNosqlDataStoreExpireKeyService()
        dataStore = KotlinNoSqlDataStore(expireKeyService)
        assertDoesNotThrow {
            runBlocking {
                withContext(Dispatchers.Default) {
                    expireKeyService.start()
                }
            }
        }
    }

    @TearDown
    internal fun tearDown() {
        KotlinNoSqlDataStore.stringValuesHashMap = HashMap()
    }

    @Benchmark
    internal fun set() {
        dataStore.set(key, value)
    }

    @Benchmark
    internal fun setEx() {
        dataStore.set(key, value, 1)
    }

    @Benchmark
    internal fun del() {
        dataStore.set(key, value)
        dataStore.del(key)
    }

    @Benchmark
    internal fun zAdd() {
        dataStore.zAdd(key, (0..10).random(), value)
    }

    @Benchmark
    internal fun zCard() {
        dataStore.zAdd(key, (0..10).random(), value)
        dataStore.zCard(key)
    }

    @Benchmark
    internal fun zRank() {
        dataStore.zAdd(key, (0..10).random(), value)
        dataStore.zRank(key, value)
    }

    @Benchmark
    internal fun zRange() {
        dataStore.zAdd(key, (0..10).random(), value)
        dataStore.zRange(key, 0, -1)
    }
}