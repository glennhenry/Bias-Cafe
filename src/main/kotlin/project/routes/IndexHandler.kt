package project.routes

import encore.context.ServerContext
import encore.datastore.collection.Topic
import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard.NoAuthGuard
import encore.route.handle
import encore.time.TimeCenter
import encore.utils.identifier.Ids
import encore.utils.types.okOrNull
import encore.utils.types.onFail
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import project.Members
import java.text.SimpleDateFormat

data class ExampleTemplateData(
    val time: String = "",
    val bias: String = "",
    val posts: List<String> = emptyList()
)

class IndexHandler(private val serverContext: ServerContext) : RouteHandler {
    override fun Route.install() {
        get("/") {
            val systemTime = TimeCenter.now()
            val bias = Members.all.random()

            val topics = serverContext.subunits.topic.getTopics().okOrNull()
            if (topics == null) {
                call.respond(HttpStatusCode.InternalServerError, "internal server error")
                return@get
            }

            val data = ExampleTemplateData(
                time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(systemTime),
                bias = bias,
                posts = topics.map { it.content }
            )

            call.respond(ThymeleafContent("lobby", mapOf("data" to data)))
        }

        get("/cafe") {
            val systemTime = TimeCenter.now()
            val bias = Members.all.random()

            val data = ExampleTemplateData(
                time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(systemTime),
                bias = bias
            )

            call.respond(ThymeleafContent("cafe", mapOf("data" to data)))
        }

        post("/cafe/post") {
            handle(call, NoAuthGuard) {
                val text = call.receiveText()

                val id = Ids.uuid()
                val topic = Topic(
                    topicId = id,
                    content = text
                )
                serverContext.subunits.topic.addTopic(topic)
                    .onFail {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to post")
                        return@handle
                    }

                Fancam.debug { "Created new topicId=$id" }
                call.respond(HttpStatusCode.OK)
            }
        }

        get("/profile") {
            val systemTime = TimeCenter.now()
            val bias = Members.all.random()

            val data = ExampleTemplateData(
                time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(systemTime),
                bias = bias
            )

            call.respond(ThymeleafContent("profile", mapOf("data" to data)))
        }
    }
}
