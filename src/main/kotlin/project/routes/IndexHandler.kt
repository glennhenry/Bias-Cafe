package project.routes

import encore.route.RouteHandler
import encore.route.guard.NoAuthGuard
import encore.route.handle
import encore.time.TimeCenter
import io.ktor.server.mustache.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import project.Members
import java.text.SimpleDateFormat

data class ExampleTemplateData(
    val time: String,
    val bias: String
)

class IndexHandler : RouteHandler {
    override fun Route.install() {
        get("/") {
            val systemTime = TimeCenter.now()
            val bias = Members.all.random()

            val data = ExampleTemplateData(
                time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(systemTime),
                bias = bias
            )

            call.respond(MustacheContent("index.html", mapOf("data" to data)))
        }
    }
}
