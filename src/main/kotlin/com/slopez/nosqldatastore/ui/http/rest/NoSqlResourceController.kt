package com.slopez.nosqldatastore.ui.http.rest

import com.slopez.nosqldatastore.application.service.NoSqlDataStore
import com.slopez.nosqldatastore.infrastructure.persistence.inmemory.KotlinNoSqlDataStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.Pattern

@RestController
@Validated
class NoSqlResourceController(@Autowired val nosqlDataStore: NoSqlDataStore) {
    @PutMapping("/{key}", params = ["value"])
    fun put(@PathVariable key: String, @Pattern(regexp = "[a-zA-Z0-9-_]+") @RequestParam value: String): String {
        return nosqlDataStore.set(key, value)
    }

    @DeleteMapping("/{key}")
    fun delete(@PathVariable key: String): Int {
        return nosqlDataStore.del(key)
    }

    @GetMapping("/{key}")
    fun get(@PathVariable key: String): String {
        if (null == nosqlDataStore.get(key)) {
            return "(nil)"
        }

        return nosqlDataStore.get(key)!!
    }
}
