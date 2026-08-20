package portal.domain.profile

import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard
import encore.utils.types.okOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import portal.context.ServerContext
import portal.domain.profile.view.model.FanProfileModel
import portal.domain.profile.view.model.OverviewModel
import portal.routes.common.Action
import portal.routes.common.ErrorModel
import portal.routes.guard.OptionalAccountGuard
import portal.routes.guard.getAccountData

/**
 * Routes for profile, which is the page `/profile/@username/{...}`.
 */
class ProfileRoutes(private val serverContext: ServerContext) : RouteHandler {
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

                val userId = serverContext.subunits.account.getUserIdByUsername(username)
                    .okOrNull() ?: run {
                    call.profileNotFound()
                    return@guard
                }

                val summary = serverContext.subunits.profile.getProfileOverview(userId).okOrNull() ?: run {
                    Fancam.warn { "UserId=$userId was previously found, but getProfileOverview failed" }
                    call.profileNotFound()
                    return@guard
                }

                val model = OverviewModel(
                    account = call.attributes.getAccountData(),
                    username = username,
                    displayName = summary.displayName,
                    avatarUrl = summary.avatarUrl,
                    country = summary.country,
                    birthday = summary.birthday,
                    bio = summary.bio
                )
                call.respond(ThymeleafContent("profile/overview", mapOf("data" to model)))
            }
        }

        get("/profile/@{username}/fan-profile") {
            guard(call, optionalAccountGuard) {
                val username = requireNotNull(call.request.pathVariables["username"])

                val userId = serverContext.subunits.account.getUserIdByUsername(username)
                    .okOrNull() ?: run {
                    call.profileNotFound()
                    return@guard
                }

                val summary = serverContext.subunits.profile.getFanProfile(userId).okOrNull() ?: run {
                    Fancam.warn { "UserId=$userId was previously found, but getFanProfile failed" }
                    call.profileNotFound()
                    return@guard
                }

                val model = FanProfileModel(
                    account = call.attributes.getAccountData(),
                    displayName = summary.displayName,
                    avatarUrl = summary.avatarUrl,
                    bias = summary.bias,
                    favoriteSong = summary.favoriteSong,
                    favoriteEra = summary.favoriteEra,
                    story = summary.story,
                )
                call.respond(ThymeleafContent("profile/fan-profile", mapOf("data" to model)))
            }
        }
    }
}

/**
 * Respond with an error page when the account profile is not found.
 */
suspend fun ApplicationCall.profileNotFound() {
    val data = ErrorModel(
        account = attributes.getAccountData(),
        title = "Profile not found",
        heading = "Profile not found",
        message = "The requested profile is not found.",
        action = Action("/", "Back to lobby")
    )
    respond(HttpStatusCode.NotFound, ThymeleafContent("error", mapOf("data" to data)))
}
