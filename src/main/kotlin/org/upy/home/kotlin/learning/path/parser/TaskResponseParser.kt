package org.upy.home.kotlin.learning.path.parser

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Component
import org.upy.home.kotlin.learning.path.dto.Task

@Component
class TaskResponseParser {

    fun apply(json: JsonNode?): Task {
        // TODO implement parsing
        return Task("123", "title", "desc")
    }
}
