package com.nfeld.jsonpathlite.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule

object JacksonUtil {
    val mapper: ObjectMapper by lazy {
        ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .registerModule(KotlinModule())
    }
}

internal fun createObjectNode() = JacksonUtil.mapper.createObjectNode()
internal fun createArrayNode() = JacksonUtil.mapper.createArrayNode()