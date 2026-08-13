package portal.routes.guard

import encore.route.guard.AuthGuard
import encore.route.guard.GuardResult
import encore.utils.types.okOrNull
import io.ktor.server.application.*
import portal.context.ServerContext
import portal.mongo.collection.UserAccount

/**
 * This guard tolerate the absence of session cookie and will always
 * returns a [GuardResult.Welcome].
 *
 * If session cookie is found and valid, it will set the [SessionAccountKey]
 * with the [UserAccount] of user.
 */
class OptionalAccountGuard(private val serverContext: ServerContext) : AuthGuard {
    override suspend fun verify(call: ApplicationCall): GuardResult {
        // no account -> no token is found; verify fails by returning null; db failure; or profile not found;
        val token = call.request.cookies["session"] ?: return GuardResult.Welcome
        val userId = serverContext.subunits.websiteSession.verify(token) ?: return GuardResult.Welcome
        val account = serverContext.subunits.account.getAccountByUserId(userId).okOrNull() ?: return GuardResult.Welcome

        call.attributes[SessionAccountKey] = account
        return GuardResult.Welcome
    }
}
