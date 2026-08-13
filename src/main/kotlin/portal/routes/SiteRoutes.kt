package portal.routes

import encore.route.RouteHandler
import encore.route.handle
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import portal.context.ServerContext
import portal.routes.common.BasicModel
import portal.routes.guard.OptionalAccountGuard
import portal.routes.guard.getAccountData

/**
 * Contains simple, uncategorized, or unimplemented site routes that don't justify
 * its own route handler.
 */
class SiteRoutes(serverContext: ServerContext) : RouteHandler {
    private val optionalAccountGuard = OptionalAccountGuard(serverContext)

    override fun Route.install() {
        get("/profile") {
            handle(call, optionalAccountGuard) {
                call.respond(
                    ThymeleafContent(
                        "profile",
                        mapOf("data" to BasicModel(call.attributes.getAccountData()))
                    )
                )
            }
        }

        get("/about") {
            handle(call, optionalAccountGuard) {
                call.respond(
                    ThymeleafContent(
                        "about",
                        mapOf("data" to BasicModel(call.attributes.getAccountData()))
                    )
                )
            }
        }

        get("/feedback") {
            handle(call, optionalAccountGuard) {
                call.respond(
                    ThymeleafContent(
                        "feedback",
                        mapOf("data" to BasicModel(call.attributes.getAccountData()))
                    )
                )
            }
        }
    }
}
