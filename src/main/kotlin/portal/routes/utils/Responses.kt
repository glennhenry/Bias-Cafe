package portal.routes.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.thymeleaf.*
import portal.routes.common.Action
import portal.routes.common.ErrorModel
import portal.routes.guard.getAccountData

/**
 * Respond with an error page of BadRequest (400).
 */
suspend fun ApplicationCall.badRequest() {
    val data = ErrorModel(
        account = attributes.getAccountData(),
        title = "Bad request",
        heading = "Bad request",
        message = "",
        action = Action("/", "Back to lobby")
    )
    respond(HttpStatusCode.BadRequest, ThymeleafContent("error", mapOf("data" to data)))
}

/**
 * Respond with an error page of InternalServerError (500).
 */
suspend fun ApplicationCall.serverError() {
    respond(HttpStatusCode.InternalServerError, mapOf("reason" to "Internal server error"))
}
