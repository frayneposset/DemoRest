package com.example.demorest

import io.kotest.core.spec.style.StringSpec
import kotlin.time.measureTimedValue

class RestClientTests : StringSpec({


    "Rest template test" {


    }

    "Web client  test" {
        val response = measureTimedValue {

        }
        // call people endpoint using restTemplate

        println("It took ${response.duration.inWholeMilliseconds} milliseconds to get the response")
    }

})
