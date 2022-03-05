package com.slopez.nosqldatastore.application.service

interface NoSqlDataStore {
    fun set(key: String, value: String) :String
    fun set(key: String, value: String, ex: Int) :String
    fun get(key: String): String?
    fun del(key: String): Int
    fun dbSize(): Int
    fun incr(key: String): Int
    fun zAdd(key: String, score: Int, member: String): Int
    fun zCard(key: String): Int
    fun zRank(key: String, member: String): Int?
    fun zRange(key: String, start: Int, stop: Int): List<String>
}