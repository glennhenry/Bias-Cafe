package project.routes

import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard.NoAuthGuard
import encore.route.handle
import encore.time.TimeCenter
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.ThymeleafContent
import project.Members
import java.text.SimpleDateFormat

data class ExampleTemplateData(
    val time: String = "",
    val bias: String = "",
    val posts: List<String> = emptyList()
)

class IndexHandler : RouteHandler {
    private val postedTexts = mutableListOf<String>()

    override fun Route.install() {
        get("/") {
            val systemTime = TimeCenter.now()
            val bias = Members.all.random()

            val data = ExampleTemplateData(
                time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(systemTime),
                bias = bias,
                posts = postedTexts
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
                Fancam.debug { "Received post text to /cafe/post: $text" }
                postedTexts.add(text)
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
