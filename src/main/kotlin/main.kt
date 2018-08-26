package todolist

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import spark.Spark.get
import spark.Request
import spark.Response
import spark.ResponseTransformer
import spark.Route

data class Task(
    val id: Long,
    val content: String,
    val done: Boolean
)

class TaskController {
    fun index(): Route = Route { request, response ->
        listOf(
            Task(1, "Go shopping at 4:00 PM.", false),
            Task(2, "Go to work at 9:00 AM.", true)
        )
    }
}

class JsonTransformer(private val objectMapper: ObjectMapper) : ResponseTransformer
{
    override fun render(model: Any?): String =
        objectMapper.writeValueAsString(model)
}

fun main(args: Array<String>) {
    val objectMapper = ObjectMapper().registerKotlinModule()
    val jsonTransformer = JsonTransformer(objectMapper)
    val taskController = TaskController()

    get("/tasks", taskController.index(), jsonTransformer)
}
