package com.slopez.nosqldatastore.service

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

@Component
class KotlinNosqlDataStoreExpireKeyService {
    suspend fun start() {
        coroutineScope {
            launch {
                Timer(true).schedule(0, 500) {
                    val filteredExpireIndex = valuesExpireIndex.filter { (key) -> key <= getNow() }
                    filteredExpireIndex.forEach { keysToExpire ->
                        keysToExpire.value.forEach { key ->
                            KotlinNoSqlDataStore.stringValuesHashMap.remove(key)
                        }
                        valuesExpireIndex.remove(keysToExpire.key)
                    }
                }
            }
        }
    }
    private val valuesExpireIndex: ConcurrentHashMap<Int, MutableList<String>> = ConcurrentHashMap()

    internal fun addKeyToExpireIndex(ex: Int, key: String) {
        val expireTimestamp = getNow()+ (ex * 1000)
        if (null == valuesExpireIndex[expireTimestamp]) {
            valuesExpireIndex[expireTimestamp] = Collections.synchronizedList(mutableListOf(key))
            return
        }

        valuesExpireIndex[expireTimestamp]?.add(key)
    }

    private fun getNow() = (System.currentTimeMillis()).toInt()
}