package org.upy.home.kotlin.learning.path.parser

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.upy.home.kotlin.learning.path.dto.Task
import org.upy.home.kotlin.learning.path.exception.TaskHandlingException

private val log = KotlinLogging.logger {}

@Component
class TaskResponseParser {

    fun apply(json: JsonNode?): Task {
        if (json == null) throw TaskHandlingException("No data found in response")
        if (log.isDebugEnabled) {
            val responseId = json.path("response_id")
            val requestId = json.path("request_id")
            log.debug { "Parsing response body: responseId=$responseId, requestId=$requestId" }
        }
        val data = json.path("data")
        val type = data.path("type")
        if (!type.textValue().equals("task"))
            throw TaskHandlingException("Unsupported entry type: '$type', 'task' expected")
        val id = data.path("id").textValue()
        val details = data.path("details")
        val title = details.path("title").textValue()
        val description = details.path("desc").textValue()
        log.debug { "Task parsed: id=$id, title=$title" }
        return Task(id, title, description)
    }
}
