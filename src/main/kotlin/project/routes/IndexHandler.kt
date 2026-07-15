package project.routes

import encore.context.ServerContext
import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard.AuthGuard
import encore.route.guard.GuardResult
import encore.route.guard.NoAuthGuard
import encore.route.handle
import encore.serialization.JSON
import encore.time.TimeCenter
import encore.utils.identifier.Ids
import encore.utils.types.Outcome
import encore.utils.types.okOrNull
import encore.utils.types.okOrThrow
import encore.utils.types.onFail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import project.Members
import project.domain.cafe.topic.Topic
import project.domain.cafe.topic.TopicDeletionOutcome
import project.domain.cafe.topic.TopicFactory
import project.domain.session.WebsiteSessionSubunit
import java.text.SimpleDateFormat

data class LobbyModel(
    val time: String = "",
    val bias: String = ""
)

data class CafeInsideModel(
    val sectionId: String,
    val topics: List<TopicModel> = emptyList()
)

data class CafeLandingModel(
    val username: String,
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

class IndexHandler(private val serverContext: ServerContext) : RouteHandler {
    private val availableSections = listOf(
        "kep1er", "kpop",
        "yujin", "xiaoting", "mashiro",
        "chaehyun", "dayeon", "hikaru",
        "bahiyyih", "youngeun", "yeseo",
        "media", "games"
    )
    private val requireAccountGuard = RequireAccountGuard(serverContext.subunits.websiteSession)

    override fun Route.install() {
        get("/") {
            val systemTime = TimeCenter.now()
            val bias = Members.all.random()

            val data = LobbyModel(
                time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(systemTime),
                bias = bias,
            )

            call.respond(ThymeleafContent("lobby", mapOf("data" to data)))
        }

        get("/cafe") {
            val username = TopicFactory.dummyAuthor()
            val spaces = serverContext.subunits.collection.getSpacesForLandingModel()
            val counts = serverContext.subunits.topic.getTopicsCountForEachSection().okOrThrow()

            val data = CafeLandingModel(
                username = username,
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

                call.respond(ThymeleafContent("cafe/create", emptyMap()))
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
            call.respond(ThymeleafContent("profile", emptyMap()))
        }

        get("/login") {
            call.respond(ThymeleafContent("login", emptyMap()))
        }
    }
}

val SessionCookieKey = AttributeKey<String>("session")

/**
 * This guard tolerate the absence of session cookie and will always
 * returns a [GuardResult.Welcome].
 *
 * If session cookie is found and valid, it will set the [SessionCookieKey]
 * with the session's token on [ApplicationCall.attributes].
 */
class OptionalAccountGuard(private val websiteSessionSubunit: WebsiteSessionSubunit) : AuthGuard {
    override suspend fun verify(call: ApplicationCall): GuardResult {
        val token = call.request.cookies["session"]
        if (token != null && websiteSessionSubunit.verify(token)) {
            call.attributes[SessionCookieKey] = token
        }
        return GuardResult.Welcome
    }
}

/**
 * This guard obligates session cookie and will return [GuardResult.GetOut]
 * if it's not found or invalid.
 *
 * It guarantees that [SessionCookieKey] is set on [ApplicationCall.attributes]
 * with the session's token.
 */
class RequireAccountGuard(private val websiteSessionSubunit: WebsiteSessionSubunit) : AuthGuard {
    override suspend fun verify(call: ApplicationCall): GuardResult {
        val token = call.request.cookies["session"]
        if (token != null && websiteSessionSubunit.verify(token)) {
            call.attributes[SessionCookieKey] = token
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
