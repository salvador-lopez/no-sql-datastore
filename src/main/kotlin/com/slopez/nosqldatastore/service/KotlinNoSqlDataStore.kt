package com.slopez.nosqldatastore.service

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

internal class StringCannotBeRepresentedAsIntegerException(message: String) : Exception(message)
internal class OperationAgainstKeyHoldingWrongTypeOfValueException(
    override val message: String = "WRONGTYPE Operation against a key holding the wrong kind of value"
) : Exception(message)

internal class KotlinNoSqlDataStore {
    suspend fun init() {
        coroutineScope {
            launch {
                Timer(true).schedule(0, 500) {
                    val filteredExpireIndex = valuesExpireIndex.filter { (key) -> key <= getNow() }
                    filteredExpireIndex.forEach { keysToExpire ->
                        keysToExpire.value.forEach { key ->
                            del(key)
                        }
                        valuesExpireIndex.remove(keysToExpire.key)
                    }
                }
            }
        }
    }

    private val stringValuesHashMap: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    private val sortedValuesHashMap: ConcurrentHashMap<String, ConcurrentHashMap<Int, MutableList<String>>> = ConcurrentHashMap()
    private val valuesExpireIndex: ConcurrentHashMap<Int, MutableList<String>> = ConcurrentHashMap()

    internal fun set(key: String, value: String) :String{
        stringValuesHashMap[key] = value
        sortedValuesHashMap.remove(key)

        return "OK"
    }

    internal fun set(key: String, value: String, ex: Int) :String{
        addKeyToExpireIndex(ex, key)

        return set(key, value)
    }

    internal fun get(key: String): String? {
        assertKeyNotExistsInSortedValuesHashMap(key)

        return stringValuesHashMap[key]
    }

    internal fun del(key: String): Int {
        if (null == stringValuesHashMap.remove(key) && null == sortedValuesHashMap.remove(key)) return 0

        return 1
    }

    internal fun dbSize(): Int {
        return stringValuesHashMap.count() + sortedValuesHashMap.count()
    }

    internal fun incr(key: String): Int {
        val currentValue = get(key)

        if (null == currentValue) {
            set(key, "0")

            return 0
        }

        val newValue: Int

        try { newValue = currentValue.toInt() + 1 } catch (e: NumberFormatException) {
            throw StringCannotBeRepresentedAsIntegerException("String '$currentValue' cannot be represented as integer")
        }

        set(key, newValue.toString())

        return newValue
    }

    internal fun zAdd(key: String, score: Int, member: String): Int {
        assertKeyNotExistsInStringValuesHashMap(key)

        val storedSortedValues = sortedValuesHashMap[key]

        if (null == storedSortedValues) {
            val sortedValues = ConcurrentHashMap<Int, MutableList<String>>()
            sortedValues[score] = Collections.synchronizedList(mutableListOf(member))
            sortedValuesHashMap[key] = sortedValues

            return 1
        }

        var previousMemberWithDifferentScoreDeleted = false
        sortedValuesHashMap[key]!!.forEach { (score, scoreMembers) ->
            if (scoreMembers.contains(member) && null != sortedValuesHashMap[key]?.get(score)) {
                sortedValuesHashMap[key]?.get(score)?.remove(member)
                val currentScoreMembersSize = sortedValuesHashMap[key]?.count()
                if (null == currentScoreMembersSize || 0 == currentScoreMembersSize) {
                    sortedValuesHashMap.remove(key)
                }
                previousMemberWithDifferentScoreDeleted = true
                return@forEach
            }
        }

        var scoreValues = storedSortedValues[score]
        if (null == scoreValues) {
            scoreValues = Collections.synchronizedList(mutableListOf(member))
            sortedValuesHashMap[key]!![score] = scoreValues

            if (previousMemberWithDifferentScoreDeleted) return 0

            return 1
        }

        scoreValues.add(member)

        return 1
    }

    internal fun zCard(key: String): Int {
        assertKeyNotExistsInStringValuesHashMap(key)

        val storedValue = sortedValuesHashMap[key] ?: return 0

        var cardinality = 0
        storedValue.forEach { (_, scoreValues) ->
            cardinality += scoreValues.count()
        }


        return cardinality
    }

    internal fun zRank(key: String, member: String): Int? {
        assertKeyNotExistsInStringValuesHashMap(key)

        val sortedValues = sortedValuesHashMap[key] ?: return null

        var memberScore : Int? = null
        sortedValues.forEach { (score, scoreValues) ->
            if (scoreValues.contains(member)) {
                memberScore = score
                return@forEach
            }
        }

        if (null != memberScore) {
            return memberScore!! - 1
        }

        return null
    }

    internal fun zRange(key: String, start: Int, stop: Int): List<String> {
        assertKeyNotExistsInStringValuesHashMap(key)

        val rangeOfElements = Collections.synchronizedList<String>(mutableListOf())

        val sortedValues = sortedValuesHashMap[key] ?: return rangeOfElements


        sortedValues.forEach { (_, scoreValues) ->
            synchronized(scoreValues) {
                val scoreValuesCount = scoreValues.count()
                var stopForThisIteration = scoreValuesCount - 1
                if (-1 < stop && stop < scoreValuesCount) {
                    stopForThisIteration = stop
                }

                rangeOfElements += scoreValues.slice(IntRange(start, stopForThisIteration))
            }
        }

        return rangeOfElements
    }

    private fun getNow() = (System.currentTimeMillis()).toInt()

    private fun addKeyToExpireIndex(ex: Int, key: String) {
        val expireTimestamp = getNow()+ (ex * 1000)
        if (null == valuesExpireIndex[expireTimestamp]) {
            valuesExpireIndex[expireTimestamp] = Collections.synchronizedList(mutableListOf(key))
            return
        }

        valuesExpireIndex[expireTimestamp]?.add(key)
    }

    private fun assertKeyNotExistsInSortedValuesHashMap(key: String) {
        if (null != sortedValuesHashMap[key]) throw OperationAgainstKeyHoldingWrongTypeOfValueException()
    }

    private fun assertKeyNotExistsInStringValuesHashMap(key: String) {
        if (null != stringValuesHashMap[key]) throw OperationAgainstKeyHoldingWrongTypeOfValueException()
    }
}
