package org.upy.home.kotlin.learning.path

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(vararg args: String) {
    runApplication<Application>(*args)
}
