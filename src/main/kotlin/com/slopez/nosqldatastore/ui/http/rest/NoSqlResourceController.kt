package com.slopez.nosqldatastore.ui.http.rest

import com.slopez.nosqldatastore.application.service.NoSqlDataStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Pattern

data class PatchWithExpireRequestBody (@field:Pattern(regexp = "[a-zA-Z0-9-_]+") val value: String, val ex: Int)

@RestController
@Validated
class NoSqlResourceController(@Autowired val nosqlDataStore: NoSqlDataStore) {
    @PutMapping("/{key}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun put(@PathVariable key: String, @Pattern(regexp = "[a-zA-Z0-9-_]+") @RequestBody value: String): String {
        return nosqlDataStore.set(key, value)
    }

    @PatchMapping("/{key}/withExpire", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchSetWithExpire(@PathVariable key: String, @Valid @RequestBody requestBody: PatchWithExpireRequestBody): String {
        return nosqlDataStore.set(key, requestBody.value, requestBody.ex)
    }

    @PatchMapping("/{key}/incr", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchIncr(@PathVariable key: String): Int {
        return nosqlDataStore.incr(key)
    }

    @DeleteMapping("/{key}")
    fun delete(@PathVariable key: String): Int {
        return nosqlDataStore.del(key)
    }

    @GetMapping("/{key}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun get(@PathVariable key: String): String {
        if (null == nosqlDataStore.get(key)) {
            return "(nil)"
        }

        return nosqlDataStore.get(key)!!
    }

    @GetMapping("/dbsize", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDbSize(): Int {
        return nosqlDataStore.dbSize()
    }
}
