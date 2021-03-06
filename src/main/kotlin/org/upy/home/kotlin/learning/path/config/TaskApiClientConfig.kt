package org.upy.home.kotlin.learning.path.config

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.upy.home.kotlin.learning.path.http.TaskApiResponseErrorFilter
import org.upy.home.kotlin.learning.path.parser.TaskResponseParser
import org.upy.home.kotlin.learning.path.service.TaskService

@Configuration
@EnableEncryptableProperties
@EnableConfigurationProperties(OAuth2ClientProperties::class)
@Import(TaskService::class, TaskResponseParser::class, TaskApiResponseErrorFilter::class)
class TaskApiClientConfig {
    @Bean
    fun clientRegistrationRepository(properties: OAuth2ClientProperties?): ReactiveClientRegistrationRepository {
        return InMemoryReactiveClientRegistrationRepository(
            ArrayList(OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties).values)
        )
    }

    @Bean
    fun authorizedClientService(
        clientRegistrationRepository: ReactiveClientRegistrationRepository
    ): ReactiveOAuth2AuthorizedClientService {
        return InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository)
    }

    @Bean
    fun oAuth2ClientManager(
        repository: ReactiveClientRegistrationRepository,
        service: ReactiveOAuth2AuthorizedClientService
    ): ReactiveOAuth2AuthorizedClientManager {
        return AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(repository, service)
    }

    @Bean
    fun taskApiResponseErrorFilter(filter: TaskApiResponseErrorFilter): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofResponseProcessor(filter::apply)
    }

    @Bean
    fun taskApiClient(
        manager: ReactiveOAuth2AuthorizedClientManager,
        @Qualifier("taskApiResponseErrorFilter") errorFilter: ExchangeFilterFunction
    ): WebClient {
        val authFilter = ServerOAuth2AuthorizedClientExchangeFilterFunction(manager)
        authFilter.setDefaultClientRegistrationId("task-api-client")
        return WebClient.builder()
            .filter(authFilter)
            .filter(errorFilter)
            .build()
    }

    // TODO error handler for WebClient
}
