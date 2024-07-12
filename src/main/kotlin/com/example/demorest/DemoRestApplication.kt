package com.example.demorest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class DemoRestApplication

fun main(args: Array<String>) {
    runApplication<DemoRestApplication>(*args)
}

data class Result(val people : List<Person>)
data class Person(val name: String, val id: Int)

@RestController
class DemoRestController(val peopleRepository: PeopleRepository) {
    @GetMapping("/people/{number}")
    fun getPeople(@PathVariable number: Int) = peopleRepository.getPeople(number)

}

@Repository
class PeopleRepository {
    fun getPeople(number: Int) =
        // generate a sequence of people where the age is incremented by 1
        Result(generateSequence(1) { it + 1 }.take(number).map {
            Person(
                name = "Person $it",
                id = it
            )
        }.toList())

}
