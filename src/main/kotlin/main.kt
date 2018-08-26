package todolist

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import spark.Spark.get

data class Task(
    val id: Long,
    val content: String,
    val done: Boolean
)

fun main(args: Array<String>) {
    val objectMapper = ObjectMapper().registerKotlinModule()
    get("/tasks", { request, response ->
        listOf(
            Task(1, "Go shopping.", false),
            Task(2, "Go to work.", true)
        )
    }, objectMapper::writeValueAsString)
}
