package todolist

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.annotation.JsonProperty
import spark.Spark.get
import spark.Spark.post
import spark.Spark.delete
import spark.Spark.path
import spark.Request
import spark.Response
import spark.ResponseTransformer
import spark.Route
import spark.Spark.halt

data class TaskCreateRequest(
    @JsonProperty("content", required = true) val content: String
)

data class Task(
    val id: Long,
    val content: String,
    val done: Boolean
)

inline fun <reified T : Any> ObjectMapper.readValue(src: ByteArray): T? =
    try {
        readValue(src, T::class.java)
    } catch(e: Exception) {
        null
    }

class TaskController(
    private val objectMapper: ObjectMapper,
    private val taskRepository: TaskRepository
) {
    fun index(): Route = Route { req, res ->
        taskRepository.findAll()
    }

    fun create(): Route = Route { req, res ->
        val request: TaskCreateRequest =
            objectMapper.readValue(req.bodyAsBytes()) ?: throw halt(400)
        val task = taskRepository.create(request.content)
        res.status(201)
        task
    }

    fun show(): Route = Route { req, res ->
        req.task ?: throw halt(404)
    }

    fun destroy(): Route = Route { req, res ->
        val task = req.task ?: throw halt(404)
        taskRepository.delete(task)
        res.status(204)
    }

    private val Request.task: Task?
        get() {
            val id = params("id").toLongOrNull()
            return id?.let(taskRepository::findById)
        }
}

class JsonTransformer(private val objectMapper: ObjectMapper) : ResponseTransformer
{
    override fun render(model: Any?): String =
        objectMapper.writeValueAsString(model)
}

class TaskRepository {
    private val tasks: MutableList<Task> = mutableListOf()
    private val maxId: Long
        get() = tasks.map(Task::id).max() ?: 0

    fun findAll(): List<Task> = tasks.toList()

    fun create(content: String): Task {
        val id = maxId + 1
        val task = Task(id, content, false)
        tasks += task
        return task
    }

    fun findById(id: Long): Task? = tasks.find { it.id == id }

    fun delete(task: Task) {
        tasks.removeIf { (id) -> id == task.id }
    }
}

fun main(args: Array<String>) {
    val objectMapper = ObjectMapper().registerKotlinModule()
    val jsonTransformer = JsonTransformer(objectMapper)
    val taskRepository = TaskRepository()
    val taskController = TaskController(objectMapper, taskRepository)

    path("/tasks") {
        get("", taskController.index(), jsonTransformer)
        post("", taskController.create(), jsonTransformer)
        get("/:id", taskController.show(), jsonTransformer)
        delete("/:id", taskController.destroy(), jsonTransformer)
    }
}
