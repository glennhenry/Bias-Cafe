package portal.domain.profile

import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import portal.context.ServerContext
import portal.routes.guard.OptionalAccountGuard

/**
 * Routes for profile, which is the page `/profile/@username/{...}`.
 */
class ProfileRoutes(serverContext: ServerContext) : RouteHandler {
    private val optionalAccountGuard = OptionalAccountGuard(serverContext)

    override fun Route.install() {
        get("/profile/{username}/{section}") {
            guard(call, optionalAccountGuard) {
                val username = requireNotNull(call.request.pathVariables["username"])
                val section = requireNotNull(call.request.pathVariables["section"])
                if (!username.startsWith("@")) {
                    call.respondRedirect("/profile/@$username/${section}", permanent = true)
                }
            }
        }

        get("/profile/@{username}/overview") {
            guard(call, optionalAccountGuard) {
                val username = requireNotNull(call.request.pathVariables["username"])
                call.respond(ThymeleafContent("profile/overview", mapOf("data" to emptyMap<String, String>())))
            }
        }

        get("/profile/@{username}/fan-profile") {
            guard(call, optionalAccountGuard) {
                val username = requireNotNull(call.request.pathVariables["username"])
                call.respond(ThymeleafContent("profile/fan-profile", mapOf("data" to emptyMap<String, String>())))
            }
        }
    }
}
