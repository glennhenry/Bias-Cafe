package portal.routes.guard

import encore.route.guard.AuthGuard
import encore.route.guard.GuardResult
import encore.utils.types.okOrNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.thymeleaf.*
import portal.context.ServerContext
import portal.domain.auth.session.WebsiteSessionSubunit
import portal.routes.common.AccountData
import portal.routes.common.Action
import portal.routes.common.ErrorModel

/**
 * This guard will reject the request and return an error page
 * if session cookie is found and valid.
 */
class MustNotHaveAccountGuard(
    private val serverContext: ServerContext,
    private val websiteSessionSubunit: WebsiteSessionSubunit
) : AuthGuard {
    override suspend fun verify(call: ApplicationCall): GuardResult {
        // no account -> no token is found; verify fails by returning null;
        val token = call.request.cookies["session"] ?: return GuardResult.Welcome
        val userId = websiteSessionSubunit.verify(token) ?: return GuardResult.Welcome
        val account = serverContext.subunits.account.getAccountByUserId(userId).okOrNull()

        val data = ErrorModel(
            account = account?.let { AccountData(account.username, account.profile.level) },
            title = "Already logged in",
            heading = "Logged in",
            message = "You are already logged in.",
            action = Action("/", "Back to lobby")
        )

        call.respond(HttpStatusCode.Forbidden, ThymeleafContent("error", mapOf("data" to data)))
        return GuardResult.Reject("cookie found and valid")
    }
}
