package org.upy.home.kotlin.learning.path.http

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.upy.home.kotlin.learning.path.exception.TaskHandlingException
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger {}

@Component
class TaskApiResponseErrorFilter {

    fun apply(response: ClientResponse): Mono<ClientResponse> {
        val statusCode = response.statusCode()
        if (statusCode.is2xxSuccessful) return Mono.just(response)
        return response.bodyToMono(String::class.java).flatMap { b -> handleError(b, statusCode) }
    }

    private fun handleError(body: String, statusCode: HttpStatus): Mono<ClientResponse> {
        val message = "Request to task API failed: statusCode=$statusCode, responseBody=$body"
        log.error { message }
        return Mono.error { TaskHandlingException(message) }
    }
}
