package com.slopez.nosqldatastore.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Warmup(iterations = 0)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
internal class KotlinNoSqlDataStoreBenchmark {
    private lateinit var key: String
    private lateinit var value: String
    private lateinit var dataStore: KotlinNoSqlDataStore
    @Setup
    internal fun setUp() {
        key = "bench-key"
        value = "bench-value"
        dataStore = KotlinNoSqlDataStore()
        runBlocking {
            withTimeout(500) {
                launch {
                    dataStore.init()
                }
            }
        }
    }

    @Benchmark
    internal fun set(): String {
        return dataStore.set(key, value)
    }

    @Benchmark
    internal fun setEx(): String {
        return dataStore.set(key, value, 1)
    }
}