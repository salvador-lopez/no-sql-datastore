package com.slopez.nosqldatastore.service

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
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
    private lateinit var expireKeysJob: Job

    @Setup
    internal fun setUp() {
        key = "bench-key"
        value = "bench-value"
        dataStore = KotlinNoSqlDataStore()
        runBlocking {
            withTimeout(500) {
                expireKeysJob = launch {
                    dataStore.init()
                }
            }
        }
    }

    @TearDown
    internal fun tearDown() {
        expireKeysJob.cancel()
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
    internal fun zRange() {
        dataStore.zAdd(key, (0..10).random(), value)
        dataStore.zRange(key, 0, -1)
    }
}