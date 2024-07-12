package com.example.demorest

import kotlinx.coroutines.*

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import kotlin.time.measureTimedValue

private const val i = 5000

@RestController
class DemoClientController(webclientBuilder: WebClient.Builder) {

    val restTemplate = RestTemplate()
    val webClient = webclientBuilder.build()
    val url = "http://localhost:8080/people/50"

    @GetMapping("/restTemplate/{number}")
    fun restTemplate(@PathVariable number: Int): Long {
        return process(::restTemplateResult)
    }


    @GetMapping("/webClient/{number}")
    suspend fun webClient(@PathVariable number: Int): Long {
        return process(::webClientResult)
    }

    private fun process(client : suspend ()-> Result): Long {
        val response = measureTimedValue {
            val channel = Channel<Result>(Channel.UNLIMITED)
            runBlocking {
                val job = launch {
                    repeat(i) {
                        launch {
                            val result = client()
                            channel.send(result)
                        }
                    }

                }

                job.join() // Wait for all requests to complete
                channel.close()
                val results = channel.receiveAsFlow().toList()
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
