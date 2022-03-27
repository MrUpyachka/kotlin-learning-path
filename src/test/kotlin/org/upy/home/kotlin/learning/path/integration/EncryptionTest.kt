package org.upy.home.kotlin.learning.path.integration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.upy.home.kotlin.learning.path.config.TaskApiClientConfig

@SpringBootTest(classes = [TaskApiClientConfig::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("task-api-mocks")
class EncryptionTest {
    @Value("\${spring.security.oauth2.client.registration.task-api-client.client-secret}")
    lateinit var secretValue: String

    @Test
    fun secretDecrypted() {
        Assertions.assertEquals("task-api-secret-test", secretValue) { "Unexpected value decrypted: $secretValue" }
    }
}
