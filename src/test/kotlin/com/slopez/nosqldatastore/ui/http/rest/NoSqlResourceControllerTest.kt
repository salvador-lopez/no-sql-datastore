package com.slopez.nosqldatastore.ui.http.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
internal class NoSqlResourceControllerTest(@Autowired var mockMvc: MockMvc) {
    private val urlPath = "/mykey"

    @Test
    internal fun `PUT invalid value should return bad request`() {
        mockMvc.put(urlPath) {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            content = "value=inval/id"
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
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            content = "value=patata"
        }
            .andExpect {
                status { isOk() }
                content { contentType("text/plain;charset=UTF-8") }
                content { string("OK") }
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
            content = "value=patata"
        }

        mockMvc.get(urlPath).andExpect {
            status { isOk() }
            content { contentType("text/plain;charset=UTF-8") }
            content { string("patata") }
        }
    }

    @Test
    internal fun `should GET (nil) if value was not previously set`() {
        mockMvc.get("/nonexistentKey").andExpect {
            status { isOk() }
            content { contentType("text/plain;charset=UTF-8") }
            content { string("(nil)") }
        }
    }
}