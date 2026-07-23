package project.routes

import encore.auth.LoginResult
import encore.context.ServerContext
import project.domain.profile.Profile
import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard
import encore.route.guard.AuthGuard
import encore.route.guard.GuardResult
import encore.route.guard.NoAuthGuard
import encore.route.handle
import encore.serialization.JSON
import encore.time.TimeCenter
import encore.utils.identifier.Ids
import encore.utils.types.*
import io.ktor.http.*
import io.ktor.http.CookieEncoding
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import io.ktor.util.*
import io.ktor.util.date.GMTDate
import kotlinx.serialization.Serializable
import project.Members
import project.domain.cafe.topic.Topic
import project.domain.cafe.topic.TopicDeletionOutcome
import project.domain.session.WebsiteSessionSubunit
import java.text.SimpleDateFormat

data class Account(
    val username: String,
    val level: Int
)

data class LobbyModel(
    val account: Account?,
    val time: String = "",
    val bias: String = ""
)

data class CafeInsideModel(
    val account: Account?,
    val sectionId: String,
    val topics: List<TopicModel> = emptyList()
)

data class CafeLandingModel(
    val account: Account?,
    val spaces: List<SpaceItem>,
    val counts: Map<String, Int>
)

// combining Space and Section
data class SpaceItem(
    val name: String,
    val sections: List<SectionItem>
)

data class SectionItem(
    val id: String,
    val name: String,
    val description: String
)

@Serializable
data class PostPayload(
    val title: String,
    val author: String,
    val content: String
)

data class TopicModel(
    val topicId: String,
    val title: String,
    val author: String,
    val content: String,
    val postedDate: Long
)

data class ErrorModel(
    val title: String,
    val heading: String,
    val message: String,
    val action: Action? = null
)

data class Action(
    val href: String,
    val text: String
)

data class LogoutModel(
    val success: Boolean
)

class IndexHandler(private val serverContext: ServerContext) : RouteHandler {
    private val availableSections = listOf(
        "kep1er", "kpop",
        "yujin", "xiaoting", "mashiro",
        "chaehyun", "dayeon", "hikaru",
        "bahiyyih", "youngeun", "yeseo",
        "media", "games"
    )
    private val optionalAccountGuard = OptionalAccountGuard(serverContext)
    private val requireAccountGuard = RequireAccountGuard(serverContext)
    private val mustNotHaveAccountGuard = MustNotHaveAccountGuard(serverContext.subunits.websiteSession)

    override fun Route.install() {
        get("/") {
            guard(call, optionalAccountGuard) {
                val systemTime = TimeCenter.now()
                val bias = Members.all.random()

                call.attributes

                val data = LobbyModel(
                    account = null,
                    time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(systemTime),
                    bias = bias,
                )

                call.respond(ThymeleafContent("lobby", mapOf("data" to data)))
            }
        }

        get("/cafe") {
            val spaces = serverContext.subunits.collection.getSpacesForLandingModel()
            val counts = serverContext.subunits.topic.getTopicsCountForEachSection().okOrThrow()

            val data = CafeLandingModel(
                account = null,
                spaces = spaces,
                counts = counts
            )

            call.respond(ThymeleafContent("cafe", mapOf("data" to data)))
        }

        get("/cafe/{section}") {
            val path = requireNotNull(call.request.pathVariables["section"])
            if (!availableSections.contains(path)) {
                call.respond(HttpStatusCode.NotFound, "Section not found")
                return@get
            }

            val topics = serverContext.subunits.topic.getTopicsOfSection(path).okOrNull()
            if (topics == null) {
                call.respond(HttpStatusCode.InternalServerError, "internal server error")
                return@get
            }

            val data = CafeInsideModel(
                account = null,
                sectionId = path,
                topics = topics.map { TopicModel(it.topicId, it.title, it.author, it.content, it.postedDate) }
            )

            call.respond(ThymeleafContent("cafe/topiclist", mapOf("data" to data)))
        }

        post("/cafe/delete") {
            val topicId = call.receiveText()

            when (val outcome = serverContext.subunits.topic.deleteTopic(topicId)) {
                is Outcome.Fail ->
                    call.respond(HttpStatusCode.InternalServerError)

                is Outcome.Ok -> when (outcome.value) {
                    TopicDeletionOutcome.Success ->
                        call.respond(HttpStatusCode.NoContent)

                    TopicDeletionOutcome.TopicNotFound ->
                        call.respond(HttpStatusCode.NotFound, "Topic not found")
                }
            }
        }

        get("/cafe/{section}/create") {
            handle(call, requireAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                if (!availableSections.contains(section)) {
                    call.respond(HttpStatusCode.NotFound, "Section not found")
                    return@handle
                }

                call.respond(ThymeleafContent("cafe/create", mapOf("data" to EmptyData)))
            }
        }

        post("/cafe/{section}/create") {
            handle(call, NoAuthGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                if (!availableSections.contains(section)) {
                    call.respond(HttpStatusCode.NotFound, "Section not found")
                    return@handle
                }

                val post = JSON.decode<PostPayload>(call.receiveText())

                val id = Ids.uuid()
                val topic = Topic(
                    topicId = id,
                    sectionId = section,
                    title = post.title,
                    author = post.author,
                    content = post.content,
                    postedDate = TimeCenter.now(),
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
            call.respond(ThymeleafContent("profile", mapOf("data" to EmptyData)))
        }

        get("/login") {
            handle(call, mustNotHaveAccountGuard) {
                call.respond(ThymeleafContent("login", mapOf("data" to EmptyData)))
            }
        }

        get("/register") {
            handle(call, mustNotHaveAccountGuard) {
                call.respond(ThymeleafContent("register", mapOf("data" to EmptyData)))
            }
        }

        get("/logout") {
            handle(call, NoAuthGuard) {
                val token = call.request.cookies["session"]
                if (token == null) {
                    val data = ErrorModel(
                        title = "Not logged in",
                        heading = "You are not logged in",
                        message = "",
                        action = Action("/", "Back to lobby")
                    )
                    call.respond(HttpStatusCode.Forbidden, ThymeleafContent("error", mapOf("data" to data)))
                    return@handle
                }

                call.respond(ThymeleafContent("logout", mapOf("data" to LogoutModel(false))))
            }
        }
    }
}

val EmptyData = emptyMap<String, String>()

fun ResponseCookies.delete(name: String) {
    append(name, "", CookieEncoding.URI_ENCODING, 0, GMTDate(), null, null)
}

// represent profile that is produced from session cookie
val SessionProfileKey = AttributeKey<Profile>("profile")

/**
 * This guard will reject the request and return an error page
 * if session cookie is found and valid.
 */
class MustNotHaveAccountGuard(private val websiteSessionSubunit: WebsiteSessionSubunit) : AuthGuard {
    override suspend fun verify(call: ApplicationCall): GuardResult {
        // no account -> no token is found; verify fails by returning null;
        val token = call.request.cookies["session"] ?: return GuardResult.Welcome
        websiteSessionSubunit.verify(token) ?: return GuardResult.Welcome

        val data = ErrorModel(
            title = "Already logged in",
            heading = "Logged in",
            message = "You are already logged in.",
            action = Action("/", "Back to lobby")
        )

        call.respond(HttpStatusCode.Forbidden, ThymeleafContent("error", mapOf("data" to data)))
        return GuardResult.Reject("cookie found and valid")
    }
}

/**
 * This guard tolerate the absence of session cookie and will always
 * returns a [GuardResult.Welcome].
 *
 * If session cookie is found and valid, it will set the [SessionProfileKey]
 * with the [Profile] of user.
 */
class OptionalAccountGuard(private val serverContext: ServerContext) : AuthGuard {
    override suspend fun verify(call: ApplicationCall): GuardResult {
        // no account -> no token is found; verify fails by returning null; db failure; or profile not found;
        val token = call.request.cookies["session"] ?: return GuardResult.Welcome
        val userId = serverContext.subunits.websiteSession.verify(token) ?: return GuardResult.Welcome
        val profile = serverContext.subunits.profile.getProfile(userId).okOrNull() ?: return GuardResult.Welcome

        call.attributes[SessionProfileKey] = profile
        return GuardResult.Welcome
    }
}

/**
 * This guard obligates session cookie and will return [GuardResult.Reject]
 * and respond with an error page if it's not found or invalid.
 *
 * It guarantees that [SessionProfileKey] is set on [ApplicationCall.attributes]
 * with the [Profile] of user.
 */
class RequireAccountGuard(private val serverContext: ServerContext) : AuthGuard {
    override suspend fun verify(call: ApplicationCall): GuardResult {
        // wrap in runCatching and ignore the result for simpler handling
        runCatching {
            // no account -> no token is found; verify fails by returning null; db failure; or profile not found;
            val token = call.request.cookies["session"]
            val userId = serverContext.subunits.websiteSession.verify(token!!)
            val profile = serverContext.subunits.profile.getProfile(userId!!).okOrNull()!!
            call.attributes[SessionProfileKey] = profile
            return GuardResult.Welcome
        }

        val data = ErrorModel(
            title = "Login required",
            heading = "You need to log in",
            message = "This action requires an account",
            action = Action("/login?return=${call.request.uri}", "Log in")
        )

        call.respond(HttpStatusCode.Forbidden, ThymeleafContent("error", mapOf("data" to data)))
        return GuardResult.Reject("User is not logged in")
    }
}

@Serializable
data class RegisterPayload(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginPayload(
    val username: String,
    val password: String
)

suspend fun ApplicationCall.serverError() {
    this.respond(HttpStatusCode.InternalServerError, mapOf("reason" to "Internal server error"))
}

const val yearInSeconds = 31_536_000L

class AuthRoutes(private val serverContext: ServerContext) : RouteHandler {
    private val optionalAccountGuard = OptionalAccountGuard(serverContext)

    override fun Route.install() {
        post("/api/register") {
            handle(call, optionalAccountGuard) {
                if (call.attributes.getOrNull(SessionProfileKey) != null) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("reason" to "You are already logged in."))
                    return@handle
                }

                val data = JSON.decode<RegisterPayload>(call.receiveText())

                if (data.username.isBlank() || data.password.isBlank() || data.email.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("reason" to "blank credentials"))
                    return@handle
                }

                val outcome = serverContext.subunits.auth
                    .register(data.username, data.password, data.email)

                if (outcome.isFail()) {
                    call.serverError()
                    return@handle
                }

                call.response.cookies.append(
                    name = "session",
                    value = serverContext.subunits.websiteSession.create(outcome.okOrThrow()),
                    maxAge = yearInSeconds,
                    path = "/"
                )

                val returnTo = call.queryParameters["return"] ?: "/"
                call.respond(HttpStatusCode.OK, mapOf("url" to returnTo))
            }
        }

        post("/api/login") {
            handle(call, optionalAccountGuard) {
                if (call.attributes.getOrNull(SessionProfileKey) != null) {
                    call.respondText("You are already logged in.")
                    return@handle
                }

                val data = JSON.decode<LoginPayload>(call.receiveText())

                if (data.username.isBlank() || data.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("reason" to "blank credentials"))
                    return@handle
                }

                val result = serverContext.subunits.auth
                    .login(data.username, data.password).okOrNull() ?: run {
                    call.serverError()
                    return@handle
                }

                when (result) {
                    is LoginResult.AccountNotFound -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("reason" to "account not found")
                        )
                    }

                    is LoginResult.InvalidCredentials -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("reason" to "wrong password")
                        )
                    }

                    is LoginResult.Success -> {
                        call.response.cookies.append(
                            name = "session",
                            value = serverContext.subunits.websiteSession.create(result.userId),
                            maxAge = yearInSeconds,
                            path = "/"
                        )

                        val returnTo = call.queryParameters["return"] ?: "/"
                        call.respond(HttpStatusCode.OK, mapOf("url" to returnTo))
                    }
                }
            }
        }

        post("/api/namecheck") {
            handle(call, NoAuthGuard) {
                val username = call.receiveText()
                if (username.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("reason" to "username is blank")
                    )
                    return@handle
                }

                val available = serverContext.subunits.auth
                    .isUsernameAvailable(username).okOrNull() ?: run {
                    call.serverError()
                    return@handle
                }

                call.respondText(if (available) "yes" else "no")
            }
        }

        post("/api/emailcheck") {
            handle(call, NoAuthGuard) {
                val email = call.receiveText()
                if (email.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("reason" to "email is blank")
                    )
                    return@handle
                }

                val available = serverContext.subunits.auth
                    .isEmailAvailable(email).okOrNull() ?: run {
                    call.serverError()
                    return@handle
                }

                call.respondText(if (available) "yes" else "no")
            }
        }

        get("/api/logout") {
            handle(call, NoAuthGuard) {
                val token = call.request.cookies["session"]
                if (token == null) {
                    val data = ErrorModel(
                        title = "Not logged in",
                        heading = "You are not logged in",
                        message = "",
                        action = Action("/", "Back to lobby")
                    )
                    call.respond(HttpStatusCode.Forbidden, ThymeleafContent("error", mapOf("data" to data)))
                    return@handle
                }

                serverContext.subunits.websiteSession.delete(token)
                call.response.cookies.delete("session")
                call.respond(ThymeleafContent("logout", mapOf("data" to LogoutModel(true))))
            }
        }
    }
}
