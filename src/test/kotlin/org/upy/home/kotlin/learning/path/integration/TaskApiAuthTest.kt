package org.upy.home.kotlin.learning.path.integration

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.upy.home.kotlin.learning.path.config.TaskApiClientConfig
import org.upy.home.kotlin.learning.path.service.TaskService
import reactor.test.StepVerifier
import java.nio.charset.StandardCharsets

@SpringBootTest(classes = [TaskApiClientConfig::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("task-api-mocks")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TaskApiAuthTest {

    @Autowired
    lateinit var service: TaskService

    companion object {
        val mockWebServer: MockWebServer = MockWebServer()

        @DynamicPropertySource
        @JvmStatic
        fun setUpMockServerLocation(registry: DynamicPropertyRegistry) {
            registry.add("task.api.location.url") { "http://localhost:" + mockWebServer.port }
        }

        @BeforeAll
        @JvmStatic
        fun setUpMocks() {
            mockWebServer.start()
        }

        @AfterAll
        @JvmStatic
        fun shutdownMocks() {
            mockWebServer.shutdown()
        }
    }

    @Test
    fun obtainingTokenBeforeRequestToApi() {
        mockWebServer.enqueue(createJsonResponse("samples/token_response.json"))
        mockWebServer.enqueue(createJsonResponse("samples/task-response.json"))

        StepVerifier.create(service.find("TSTDT-77385").log())
            .expectNextCount(1)
            .verifyComplete()

        val requestCount = mockWebServer.requestCount
        Assertions.assertEquals(2, requestCount, "Token request and task request expected")
    }

    private fun createJsonResponse(contentClasspath: String): MockResponse {
        return MockResponse()
            .setBody(readClassPathFileContent(contentClasspath))
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    private fun readClassPathFileContent(path: String): String {
        return IOUtils.toString(ClassPathResource(path).inputStream, StandardCharsets.UTF_8)
    }
}
