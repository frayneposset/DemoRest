package com.example.demorest

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import kotlin.reflect.KSuspendFunction0
import kotlin.time.measureTimedValue

@RestController
class DemoClientController(webclientBuilder: WebClient.Builder) {

    val restTemplate = RestTemplate()

    val webClient = webclientBuilder.clientConnector(
        ReactorClientHttpConnector(
            HttpClient.create(
                ConnectionProvider.builder("myConnectionPool")
                    .maxConnections(500)
                    .pendingAcquireMaxCount(500)
                    .build()
            )
        )
    ).build()

    val url = "http://localhost:8080/people/50"

    @GetMapping("/restTemplate/{numberOfSimultaneousRequests}")
    fun restTemplate(@PathVariable numberOfSimultaneousRequests: Int): Long {
        return process(::restTemplateResult, numberOfSimultaneousRequests)
    }


    @GetMapping("/webClient/{numberOfSimultaneousRequests}")
    suspend fun webClient(@PathVariable numberOfSimultaneousRequests: Int): Long {
        return process(::webClientResult, numberOfSimultaneousRequests)
    }

    private fun process(client: KSuspendFunction0<Result>, numberOfSimultaneousRequests: Int): Long {
        val response = measureTimedValue {
            val channel = Channel<Result>(Channel.UNLIMITED)

            // Launch a coroutine for each request and send the results to the channel
            // This is basically firing off 500 requests in parallel

            runBlocking {
                val job = launch {
                    repeat(numberOfSimultaneousRequests) {
                        launch {
                            val result = client()
                            channel.send(result)
                        }
                    }

                }

                job.join() // Wait for all requests to complete
                channel.close()

                // Receive the results from the channel
                channel.receiveAsFlow().toList()
            }
        }

        println("WebClient took ${response.duration.inWholeMilliseconds} milliseconds to get the response")
        return response.duration.inWholeMilliseconds
    }

    private suspend fun webClientResult(): Result {
        val result = webClient.get()
            .uri(url)
            .retrieve()
            .awaitBody<Result>()
        return result
    }

    private suspend fun restTemplateResult() = restTemplate.getForEntity(url, Result::class.java).body ?: Result(
        emptyList()
    )

}
