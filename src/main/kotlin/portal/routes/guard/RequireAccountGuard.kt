package portal.routes.guard

import encore.route.guard.AuthGuard
import encore.route.guard.GuardResult
import encore.utils.types.okOrNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.thymeleaf.*
import portal.context.ServerContext
import portal.mongo.collection.UserAccount
import portal.routes.common.Action
import portal.routes.common.ErrorModel

/**
 * This guard obligates session cookie and will return [GuardResult.Reject]
 * and respond with an error page if it's not found or invalid.
 *
 * It guarantees that [SessionAccountKey] is set on [ApplicationCall.attributes]
 * with the [UserAccount] of user.
 */
class RequireAccountGuard(private val serverContext: ServerContext) : AuthGuard {
    override suspend fun verify(call: ApplicationCall): GuardResult {
        // wrap in runCatching and ignore the result for simpler handling
        runCatching {
            // no account -> no token is found; verify fails by returning null; db failure; or profile not found;
            val token = call.request.cookies["session"]
            val userId = serverContext.subunits.websiteSession.verify(token!!)
            val account = serverContext.subunits.account.getAccountByUserId(userId!!).okOrNull()!!
            call.attributes[SessionAccountKey] = account
            return GuardResult.Welcome
        }

        val data = ErrorModel(
            account = null,
            title = "Login required",
            heading = "You need to log in",
            message = "This action requires an account",
            action = Action("/login?return=${call.request.uri}", "Log in")
        )

        call.respond(HttpStatusCode.Forbidden, ThymeleafContent("error", mapOf("data" to data)))
        return GuardResult.Reject("User is not logged in")
    }
}
