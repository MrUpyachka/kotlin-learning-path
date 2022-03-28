package org.upy.home.kotlin.learning.path.integration

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.util.SocketUtils
import org.upy.home.kotlin.learning.path.config.TaskApiClientConfig
import org.upy.home.kotlin.learning.path.dto.Task
import org.upy.home.kotlin.learning.path.service.TaskService
import reactor.test.StepVerifier
import java.nio.charset.StandardCharsets

@SpringBootTest(classes = [TaskApiClientConfig::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("task-api-mocks")
class TaskFetchingTest {

    @Autowired
    lateinit var service: TaskService
    lateinit var mockWebServer: MockWebServer

    companion object {
        const val testTaskId = "TSTDT-77385"
        const val tokenRsContentPath = "samples/token-response.json"
        const val fetchRsContentPath = "samples/task-response.json"
        val mockServerPort: Int = SocketUtils.findAvailableTcpPort() // deprecated but test utils not implemented
        private val taskApiLocationUrl: String = "http://localhost:$mockServerPort"

        @DynamicPropertySource
        @JvmStatic
        fun setUpMockServerLocation(registry: DynamicPropertyRegistry) {
            registry.add("task.api.location.url") { taskApiLocationUrl }
        }
    }

    @BeforeEach
    fun setUpMocks() {
        mockWebServer = MockWebServer()
        mockWebServer.start(mockServerPort)
    }

    @AfterEach
    fun shutdownMocks() {
        mockWebServer.shutdown()
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun obtainingTokenBeforeRequestToApi() {
        mockWebServer.enqueue(createJsonResponse(tokenRsContentPath))
        mockWebServer.enqueue(createJsonResponse(fetchRsContentPath))

        StepVerifier.create(service.find(testTaskId)).expectNextCount(1).verifyComplete()

        val requestCount = mockWebServer.requestCount
        Assertions.assertEquals(2, requestCount, "Token request and task request expected")
        val tokenRequest = mockWebServer.takeRequest()
        val expectedUrlSuffix = "$mockServerPort/api/oauth2/token"
        val actualUrl = tokenRequest.requestUrl.toString()
        // just sanity check that it was request for token, not something else
        Assertions.assertTrue(
            actualUrl.endsWith(
                expectedUrlSuffix
            )
        ) { "Unexpected token request URL: '$actualUrl', expectedUrlSuffix='$expectedUrlSuffix'" }
        // not checking the rest of request as it provided by spring
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun correctRequestDetails() {
        mockWebServer.enqueue(createJsonResponse(tokenRsContentPath))
        mockWebServer.enqueue(createJsonResponse(fetchRsContentPath))

        StepVerifier.create(service.find(testTaskId)).expectNextCount(1).verifyComplete()

        mockWebServer.takeRequest() // ignore first one as it was token request
        val request = mockWebServer.takeRequest()
        Assertions.assertEquals(HttpMethod.GET.name, request.method, "Unexpected request HTTP method")
        val expectedUrlSuffix = "$mockServerPort/api/task?id=$testTaskId"
        val actualUrl = request.requestUrl.toString()
        Assertions.assertTrue(
            actualUrl.endsWith(
                expectedUrlSuffix
            )
        ) { "Unexpected request URL: '$actualUrl', expectedUrlSuffix='$expectedUrlSuffix'" }
        val acceptHeader = request.getHeader(HttpHeaders.ACCEPT)
        val expectedPrefix = MediaType.APPLICATION_JSON_VALUE
        Assertions.assertTrue(
            acceptHeader?.startsWith(expectedPrefix) ?: false
        ) { "Unexpected Accept header value: '$acceptHeader', expected to start with '$expectedPrefix'" }
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        Assertions.assertEquals(
            "Bearer NTUxNjdiODUtMGE3Yy00OTFkLWI0NDUtNzdiMjdjYmM5YTI2", authHeader,
            "Unexpected Authorization header"
        )
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun taskParsing() {
        mockWebServer.enqueue(createJsonResponse(tokenRsContentPath))
        mockWebServer.enqueue(createJsonResponse(fetchRsContentPath))

        StepVerifier.create(service.find(testTaskId))
            .assertNext(::assertTaskParsed)
            .verifyComplete()
    }

    private fun assertTaskParsed(task: Task) {
        Assertions.assertEquals(testTaskId, task.id, "Unexpected task id")
        Assertions.assertEquals("Implement tests for response handling", task.title, "Unexpected task title")
        Assertions.assertEquals("TBA", task.description, "Unexpected task description")
    }

    private fun createJsonResponse(contentClasspath: String): MockResponse {
        return MockResponse().setBody(readClassPathFileContent(contentClasspath))
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    private fun readClassPathFileContent(path: String): String {
        return IOUtils.toString(ClassPathResource(path).inputStream, StandardCharsets.UTF_8)
    }
}
