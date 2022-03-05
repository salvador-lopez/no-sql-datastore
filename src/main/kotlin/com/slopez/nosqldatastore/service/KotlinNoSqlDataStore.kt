package com.slopez.nosqldatastore.service

import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

internal class StringCannotBeRepresentedAsIntegerException(message: String) : Exception(message)
internal class OperationAgainstKeyHoldingWrongTypeOfValueException(
    override val message: String = "WRONGTYPE Operation against a key holding the wrong kind of value"
) : Exception(message)

@Component
class KotlinNoSqlDataStore(val expireKeyService: KotlinNosqlDataStoreExpireKeyService) {
    companion object {
        internal var stringValuesHashMap: HashMap<String, String> = HashMap()
    }

    private val sortedValuesHashMap: HashMap<String, ConcurrentHashMap<Int, MutableList<String>>> = HashMap()

    internal fun set(key: String, value: String) :String {
        stringValuesHashMap[key] = value
        sortedValuesHashMap.remove(key)

        return "OK"
    }

    internal fun set(key: String, value: String, ex: Int) :String {
        expireKeyService.addKeyToExpireIndex(ex, key)

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
        scoreValues.sort()

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

    private fun assertKeyNotExistsInSortedValuesHashMap(key: String) {
        if (null != sortedValuesHashMap[key]) throw OperationAgainstKeyHoldingWrongTypeOfValueException()
    }

    private fun assertKeyNotExistsInStringValuesHashMap(key: String) {
        if (null != stringValuesHashMap[key]) throw OperationAgainstKeyHoldingWrongTypeOfValueException()
    }
}
