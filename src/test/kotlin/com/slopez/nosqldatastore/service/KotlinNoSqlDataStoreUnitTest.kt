package com.slopez.nosqldatastore.service

import kotlinx.coroutines.*

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KotlinNoSqlDataStoreUnitTest {
    private val key: String = "key"
    private val value: String = "value"
    private val score: Int = 1
    private val start: Int = 0
    private val stop: Int = -1
    private lateinit var dataStore: KotlinNoSqlDataStore

    @BeforeEach
    internal fun setUp() {
        dataStore = KotlinNoSqlDataStore()

        assertDoesNotThrow {
            runBlocking {
                withTimeout(2000) {
                    launch {
                        dataStore.init()
                    }
                }
            }
        }
    }

    @Test
    internal fun shouldSetValueAndReturnOK() {
        assertEquals("OK", dataStore.set(key, value))
    }

    @Test
    internal fun shouldSetWithoutConcurrencyErrors() = runBlocking {
        assertDoesNotThrow {
            for (i in 0..10000) {
                launch { dataStore.set(key, value+i) }
            }
        }
    }

    @Test
    internal fun shouldSetExWithoutConcurrencyErrors() = runBlocking {
        assertDoesNotThrow {
            for (i in 0..10000) {
                launch { dataStore.set(key, value+i, 1) }
            }
        }
    }

    @Test
    internal fun shouldRemoveWhenExpires() {
        dataStore.set(key, value, 1)
        Thread.sleep(1500)
        assertNull(dataStore.get(key))
    }

    @Test
    internal fun shouldReturnNilWhenValueDoesNotExists() {
        assertNull(dataStore.get(key))
    }

    @Test
    internal fun shouldReturnValueWhenWasPreviouslySet() {
        dataStore.set(key, value)
        assertSame(value, dataStore.get(key))
    }

    @Test
    internal fun shouldReturnValueWhenWasPreviouslySetAndItsNotExpired() {
        dataStore.set(key, value, 1)
        assertSame(value, dataStore.get(key))
    }

    @Test
    internal fun shouldThrowOperationAgainstKeyHoldingWrongTypeOfValueExceptionWhenCallToGetInAKeyHoldingNonStringValue() {
        val exception = assertThrows<OperationAgainstKeyHoldingWrongTypeOfValueException> {
            dataStore.zAdd(key, score, value)
            dataStore.get(key)
        }
        assertOperationAgainstKeyHoldingWrongTypeOfValueExceptionMessage(exception)
    }

    @Test
    internal fun shouldReturnZeroWhenDeleteKeyThatDoesNotExists() {
        assertEquals(0, dataStore.del(key))
    }

    @Test
    internal fun shouldDeleteAndReturnOneWhenKeyWasPreviouslySet() {
        dataStore.set(key, value)
        assertEquals(1, dataStore.del(key))
        assertNull(dataStore.get(key))
    }

    @Test
    internal fun shouldDeleteAndReturnOneWhenKeyWasPreviouslyZAdd() {
        dataStore.zAdd(key, score, value)
        assertEquals(1, dataStore.del(key))
        assertEquals(0, dataStore.zCard(key))
    }

    @Test
    internal fun shouldReturnZeroDBSizeWhenThereIsNoRecords() {
        assertEquals(0, dataStore.dbSize())
    }

    @Test
    internal fun shouldReturnTheExpectedDBSize() {
        for (i in 0..8) {
            dataStore.set(key+i, value+i)
        }
        dataStore.zAdd(key, score, value)

        assertEquals(10, dataStore.dbSize())
    }

    @Test
    internal fun shouldIncrementTheStringRepresentationOfAnIntegerPreviouslySet() {
        dataStore.set(key, "10")

        assertEquals(11, dataStore.incr(key))
    }

    @Test
    internal fun shouldReturnZeroWhenCallToIncrAndTheKeyWasNotPreviouslySet() {
        assertEquals(0, dataStore.incr(key))
    }

    @Test
    internal fun shouldThrowAStringCannotBeRepresentedAsAnIntegerExceptionWhenCallToIncr() {
        val exception = assertThrows<StringCannotBeRepresentedAsIntegerException> {
            dataStore.set(key, value)
            dataStore.incr(key)
        }
        assertEquals("String '$value' cannot be represented as integer", exception.message)
    }

    @Test
    internal fun shouldReturnOneWhenCallToZADDWithANewMember() {
        assertEquals(1, dataStore.zAdd(key, score, value))
    }

    @Test
    internal fun shouldReturnZeroWhenCallToZADDOnlyToUpdateScore() {
        dataStore.zAdd(key, score, value)
        assertEquals(0, dataStore.zAdd(key, score + 1, value))
    }

    @Test
    internal fun shouldReturnZeroWhenCallToZADDOnlyToUpdateScoreAndTheNewScoreHaveOtherMembers() {
        dataStore.zAdd(key, score, value)
        dataStore.zAdd(key, score, "value2")
        assertEquals(0, dataStore.zAdd(key, score + 1, value))
    }

    @Test
    internal fun shouldReturnOneWhenCallToZADDWithANewMemberInAScoreWithMoreExistingMembers() {
        dataStore.zAdd(key, score, value)
        assertEquals(1, dataStore.zAdd(key, score, "value2"))
    }

    @Test
    internal fun shouldThrowOperationAgainstKeyHoldingWrongTypeOfValueExceptionWhenCallToZADDInAKeyHoldingStringValue() {
        val exception = assertThrows<OperationAgainstKeyHoldingWrongTypeOfValueException> {
            dataStore.set(key, value)
            dataStore.zAdd(key, score, value)
        }
        assertOperationAgainstKeyHoldingWrongTypeOfValueExceptionMessage(exception)
    }

    @Test
    internal fun shouldReturnZeroWhenCallToZCardAndKeyDoesNotExists() {
        assertEquals(0, dataStore.zCard(key))
    }

    @Test
    internal fun shouldThrowOperationAgainstKeyHoldingWrongTypeOfValueExceptionWhenCallToZCardInAKeyHoldingStringValue() {
        val exception = assertThrows<OperationAgainstKeyHoldingWrongTypeOfValueException> {
            dataStore.set(key, value)
            dataStore.zCard(key)
        }
        assertOperationAgainstKeyHoldingWrongTypeOfValueExceptionMessage(exception)
    }

    @Test
    internal fun shouldReturnThreeWhenCallToZCardAndTheSortedSetsWithThreeElementsWasPreviouslyAdded() {
        dataStore.zAdd(key, score, value)
        dataStore.zAdd(key, score + 1, "value2")
        dataStore.zAdd(key, score, "value3")
        assertEquals(3, dataStore.zCard(key))
    }

    @Test
    internal fun shouldReturnTwoWhenCallToZRankAndTheScoreMemberWasInTheThirdPosition() {
        dataStore.zAdd(key, 1, "one")
        dataStore.zAdd(key, 2, "two")
        dataStore.zAdd(key, 3, "three")
        assertEquals(2, dataStore.zRank(key, "three"))
    }

    @Test
    internal fun shouldReturnZeroWhenCallToZRankAndTheMemberHaveTheLowerScore() {
        dataStore.zAdd(key, 1, "one")
        dataStore.zAdd(key, 2, "two")
        dataStore.zAdd(key, 3, "three")
        assertEquals(0, dataStore.zRank(key, "one"))
    }

    @Test
    internal fun shouldReturnNullWhenCallToZRankAndTheKeyDoesNotExists() {
        assertNull(dataStore.zRank(key, value))
    }

    @Test
    internal fun shouldReturnNullWhenCallToZRankAndTheMemberDoesNotExistInTheSortedSet() {
        dataStore.zAdd(key, score, value)
        assertNull(dataStore.zRank(key, "anotherValue"))
    }

    @Test
    internal fun shouldThrowOperationAgainstKeyHoldingWrongTypeOfValueExceptionWhenCallToZRankInAKeyHoldingStringValue() {
        val exception = assertThrows<OperationAgainstKeyHoldingWrongTypeOfValueException> {
            dataStore.set(key, value)
            dataStore.zRank(key, value)
        }
        assertOperationAgainstKeyHoldingWrongTypeOfValueExceptionMessage(exception)
    }

    @Test
    internal fun shouldThrowOperationAgainstKeyHoldingWrongTypeOfValueExceptionWhenCallToZRangeInAKeyHoldingStringValue() {
        val exception = assertThrows<OperationAgainstKeyHoldingWrongTypeOfValueException> {
            dataStore.set(key, value)
            dataStore.zRange(key, start, stop)
        }
        assertOperationAgainstKeyHoldingWrongTypeOfValueExceptionMessage(exception)
    }

    @Test
    internal fun shouldReturnAnEmptyArrayWhenCallToZRangeInAKeyThatIsNotSet() {
        val expectedResult = listOf<String>()
        assertTrue(expectedResult == dataStore.zRange(key, start, stop))
    }

    @Test
    internal fun shouldReturnAllElementsOfTheSortedSetWhenCallToZRangeFromZeroToMinusOne() {
        dataStore.zAdd(key, 1, "one")
        dataStore.zAdd(key, 2, "two")
        dataStore.zAdd(key, 3, "three")

        val expectedResult = listOf("one", "two", "three")
        assertTrue(expectedResult == dataStore.zRange(key, start, stop))
    }

    @Test
    internal fun shouldReturnAllElementsOfTheSortedSetWhenCallToZRangeFromZeroToThePositionOfTheLastElement() {
        dataStore.zAdd(key, 1, "one-one")
        dataStore.zAdd(key, 1, "one-two")
        dataStore.zAdd(key, 2, "two")
        dataStore.zAdd(key, 3, "three")

        val stop = 1
        val expectedResult = listOf("one-one", "one-two", "two", "three")
        assertTrue(expectedResult == dataStore.zRange(key, start, stop))
    }

    @Test
    internal fun shouldReturnFirstElementsOfTheSortedSetWhenCallToZRangeWithStartAndEndInFirstPosition() {
        dataStore.zAdd(key, 1, "one")

        val expectedResult = listOf("one")
        assertTrue(expectedResult == dataStore.zRange(key, start, start))
    }

    private fun assertOperationAgainstKeyHoldingWrongTypeOfValueExceptionMessage(exception: OperationAgainstKeyHoldingWrongTypeOfValueException) {
        assertEquals("WRONGTYPE Operation against a key holding the wrong kind of value", exception.message)
    }
}