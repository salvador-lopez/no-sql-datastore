package com.slopez.nosqldatastore.ui.http.rest

import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
internal class NoSqlResourceControllerIntegrationTest(@Autowired var mockMvc: MockMvc) {
    private val urlPath = "/mykey"

    @Test
    internal fun `PUT invalid value should return bad request`() {
        mockMvc.put(urlPath) {
            contentType = MediaType.APPLICATION_JSON
            content = "inval/id"
        }
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("{'message':'Bad Request','details':'put.value: must match \"[a-zA-Z0-9-_]+\"'}") }
            }

    }

    @Test
    internal fun `should PUT new value and return OK`() {
        mockMvc.put(urlPath) {
            contentType = MediaType.APPLICATION_JSON
            content = "patata"
        }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { string("OK") }
            }

    }

    @Test
    internal fun `should PATCH invalid value with expire time and return Bad Request`() {
        val jsonContent = JSONObject()
        jsonContent.put("value","inval/id").put("ex",1)

        mockMvc.patch("$urlPath/withExpire") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonContent
        }
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("{'message':'Bad Request','details':'[value: must match \"[a-zA-Z0-9-_]+\"]'}") }
            }
    }

    @Test
    internal fun `should PATCH new value with expire time and return OK`() {
        val jsonContent = JSONObject()
        jsonContent.put("value","patata").put("ex",1)

        mockMvc.patch("$urlPath/withExpire") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonContent
        }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { string("OK") }
            }
    }

    @Test
    internal fun `should PATCH the key with incr operation and return 409 conflict when key holds not integer value`() {
        mockMvc.put(urlPath) {
            contentType = MediaType.APPLICATION_JSON
            content = "value"
        }

        mockMvc.patch("$urlPath/incr").andExpect {
            status { isConflict() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("{\"message\":\"Domain conflict: \",\"details\":\"String 'value' cannot be represented as integer\"}") }
        }
    }

    @Test
    internal fun `should PATCH the key with incr operation and return 200 OK with expected response`() {
        mockMvc.put(urlPath) {
            contentType = MediaType.APPLICATION_JSON
            content = "5"
        }

        mockMvc.patch("$urlPath/incr").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("6") }
        }
    }

    @Test
    internal fun `should DELETE value previously set and return 1`() {
        mockMvc.delete(urlPath).andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("1") }
        }
    }

    @Test
    internal fun `should not DELETE not existent value and return 0`() {
        mockMvc.delete(urlPath)

        mockMvc.delete(urlPath).andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("0") }
        }
    }

    @Test
    internal fun `should GET value previously set`() {
        mockMvc.put(urlPath) {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            content = "patata"
        }

        mockMvc.get(urlPath).andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("patata") }
        }
    }

    @Test
    internal fun `should GET (nil) if value was not previously set`() {
        mockMvc.get("/nonexistentKey").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("(nil)") }
        }
    }

    @Test
    internal fun `should GET empty dbSize`() {
        mockMvc.delete(urlPath)

        mockMvc.get("/dbsize").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("0") }
        }
    }

    @Test
    internal fun `should GET dbSize as expected`() {
        mockMvc.put(urlPath) {
            contentType = MediaType.APPLICATION_JSON
            content = "value"
        }

        mockMvc.get("/dbsize").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("1") }
        }
    }
}