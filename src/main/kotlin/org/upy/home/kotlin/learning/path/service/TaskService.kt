package org.upy.home.kotlin.learning.path.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.upy.home.kotlin.learning.path.dto.Task
import org.upy.home.kotlin.learning.path.parser.TaskResponseParser
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger {}

@Component
class TaskService constructor(
    @Value("\${task.api.endpoint.create.url}") val endpointUrl: String,
    @Qualifier("taskApiClient") val client: WebClient,
    val parser: TaskResponseParser
) {

    fun find(id: String): Mono<Task> {
        log.info { "Fetching task: id=$id" }
        return client.get()
            .uri(endpointUrl) { builder -> builder.queryParam("id", id).build() }
            .headers { headers -> headers.accept = listOf(MediaType.APPLICATION_JSON) }
            .retrieve()
            .toEntity(JsonNode::class.java)
            .map { e -> parser.apply(e.body) }
    }
}
