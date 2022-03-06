package com.slopez.nosqldatastore.application.service

interface NoSqlDataStore {
    fun set(key: String, value: String) :String
    fun set(key: String, value: String, ex: Int) :String
    @Throws(OperationAgainstKeyHoldingWrongTypeOfValueException::class)
    fun get(key: String): String?
    fun del(key: String): Int
    fun dbSize(): Int
    @Throws(StringCannotBeRepresentedAsIntegerException::class)
    fun incr(key: String): Int
    @Throws(OperationAgainstKeyHoldingWrongTypeOfValueException::class)
    fun zAdd(key: String, score: Int, member: String): Int
    @Throws(OperationAgainstKeyHoldingWrongTypeOfValueException::class)
    fun zCard(key: String): Int
    @Throws(OperationAgainstKeyHoldingWrongTypeOfValueException::class)
    fun zRank(key: String, member: String): Int?
    @Throws(OperationAgainstKeyHoldingWrongTypeOfValueException::class)
    fun zRange(key: String, start: Int, stop: Int): List<String>
}