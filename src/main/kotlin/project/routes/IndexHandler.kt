package project.routes

import encore.auth.LoginResult
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
import encore.utils.identifier.shortUuid
import encore.utils.types.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import project.Members
import project.context.ServerContext
import project.domain.cafe.toUrlSlug
import project.domain.cafe.topic.Topic
import project.domain.cafe.topic.TopicDeletionOutcome
import project.domain.cafe.topic.reply.Comment
import project.domain.cafe.topic.reply.Reply
import project.domain.session.WebsiteSessionSubunit
import project.mongo.collection.UserAccount
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

data class WriteTopicModel(
    val account: Account?,
    val sectionName: String
)

@Serializable
data class PostPayload(
    val title: String,
    val content: String
)

data class TopicListModel(
    val account: Account?,
    val sectionId: String,
    val topics: List<TopicModel> = emptyList()
)

data class TopicModel(
    val topicId: String,
    val link: String,
    val title: String,
    val authorName: String,
    val replyCount: Int,
    val postedDate: Long
)

data class TopicViewModel(
    val account: Account?,
    val sectionName: String,
    val topicId: String,
    val topic: TopicData,
    val replies: List<ReplyData>
)

data class TopicData(
    val title: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String,
    val postedDate: Long,
    val content: String
)

data class ReplyData(
    val replyId: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String,
    val postedDate: Long,
    val content: String,
    val comments: List<CommentData>
)

data class CommentData(
    val commentId: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String,
    val postedDate: Long,
    val content: String
)

data class ErrorModel(
    val account: Account?,
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
    val account: Account?
)

data class ProfileModel(
    val account: Account?
)

data class RegisterModel(
    val account: Account?
)

data class LoginModel(
    val account: Account?
)

@Serializable
data class ReplyPayload(
    val reply: String
)

@Serializable
data class CommentPayload(
    val comment: String
)

class IndexHandler(private val serverContext: ServerContext) : RouteHandler {
    private val sections = mapOf(
        "kep1er" to "Kep1er Discussion",
        "kpop" to "K-pop Discussion",
        "yujin" to "Yujin's Space",
        "xiaoting" to "Xiaoting's Space",
        "mashiro" to "Mashiro's Space",
        "chaehyun" to "Chaehyun's Space",
        "dayeon" to "Dayeon's Space",
        "hikaru" to "Hikaru's Space",
        "hiyyih" to "Hiyyih's Space",
        "youngeun" to "Youngeun's Space",
        "yeseo" to "Yeseo's Space",
        "media" to "Media",
        "games" to "Games"
    )
    private val optionalAccountGuard = OptionalAccountGuard(serverContext)
    private val requireAccountGuard = RequireAccountGuard(serverContext)
    private val mustNotHaveAccountGuard = MustNotHaveAccountGuard(serverContext, serverContext.subunits.websiteSession)

    override fun Route.install() {
        get("/") {
            guard(call, optionalAccountGuard) {
                val systemTime = TimeCenter.now()
                val bias = Members.all.random()

                val data = LobbyModel(
                    account = call.attributes.getProfileAndMapToAccountModel(),
                    time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(systemTime),
                    bias = bias,
                )

                call.respond(ThymeleafContent("lobby", mapOf("data" to data)))
            }
        }

        get("/cafe") {
            guard(call, optionalAccountGuard) {
                val spaces = serverContext.subunits.collection.getSpacesForLandingModel()
                val counts = serverContext.subunits.topic.getTopicsCountForEachSection().okOrThrow()

                val data = CafeLandingModel(
                    account = call.attributes.getProfileAndMapToAccountModel(),
                    spaces = spaces,
                    counts = counts
                )

                call.respond(ThymeleafContent("cafe", mapOf("data" to data)))
            }
        }

        get("/cafe/{section}") {
            guard(call, optionalAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])

                if (!sections.contains(section)) {
                    call.sectionNotFound()
                    return@guard
                }

                val topics = serverContext.subunits.topic.getTopicsOfSection(section).okOrNull()
                if (topics == null) {
                    call.respond(HttpStatusCode.InternalServerError, "internal server error")
                    return@guard
                }

                val authorIds = mutableListOf<String>()
                val topicIds = mutableListOf<String>()

                for ((topicId, _, _, authorId) in topics) {
                    authorIds.add(authorId)
                    topicIds.add(topicId)
                }

                val summaries = serverContext.subunits.profile
                    .getUserSummaries(authorIds)
                    .okOrNull() ?: run {
                    call.respond(HttpStatusCode.InternalServerError, "internal server error")
                    return@guard
                }

                val replyCounts = serverContext.subunits.reply
                    .getReplyCounts(topicIds)
                    .okOrNull() ?: run {
                    call.respond(HttpStatusCode.InternalServerError, "internal server error")
                    return@guard
                }

                val data = TopicListModel(
                    account = call.attributes.getProfileAndMapToAccountModel(),
                    sectionId = section,
                    topics = topics.map {
                        TopicModel(
                            topicId = it.topicId,
                            link = "${section}/${it.topicId.shortUuid()}/${it.title.toUrlSlug()}",
                            title = it.title,
                            authorName = summaries[it.authorId]?.displayName ?: run {
                                Fancam.warn { "authorName of ${it.topicId} (title=${it.title}) is null" }
                                "<authorName:null>"
                            },
                            replyCount = replyCounts[it.topicId] ?: 0,
                            postedDate = it.postedDate
                        )
                    }
                )

                call.respond(ThymeleafContent("cafe/topiclist", mapOf("data" to data)))
            }
        }

        get("/cafe/{section}/{id}/{title}") {
            guard(call, optionalAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                val id = requireNotNull(call.request.pathVariables["id"])
                val title = requireNotNull(call.request.pathVariables["title"])

                if (!sections.contains(section)) {
                    call.sectionNotFound()
                    return@guard
                }

                val topic = serverContext.subunits.topic.getTopicByShortId(id).okOrNull()
                if (topic == null) {
                    call.topicNotFound()
                    return@guard
                }

                // title from link is different than title in DB: redirect this
                val currentSlug = topic.title.toUrlSlug()
                if (title != currentSlug) {
                    call.respondRedirect(
                        url = "/cafe/$section/$id/$currentSlug",
                        permanent = true
                    )
                    return@guard
                }
                val authors = mutableListOf(topic.authorId)

                val replies = serverContext.subunits.reply.getRepliesUnder(topic.topicId).okOrNull() ?: emptyList()
                for ((_, _, authorId, _, _, comments) in replies) {
                    authors.add(authorId)
                    for ((_, authorId2) in comments) {
                        authors.add(authorId2)
                    }
                }

                val summaries = serverContext.subunits.profile.getUserSummaries(authors.distinct()).okOrThrow()

                val topicAuthorSummary = summaries[topic.authorId]
                val data = TopicViewModel(
                    account = call.attributes.getProfileAndMapToAccountModel(),
                    sectionName = requireNotNull(sections[section]) { "Ensure sections contains $section" },
                    topicId = topic.topicId,
                    topic = TopicData(
                        title = topic.title,
                        authorDisplayName = topicAuthorSummary?.displayName ?: "<topicAuthor.displayName:null>",
                        authorAvatarUrl = topicAuthorSummary?.avatarUrl ?: "<topicAuthor.avatarUrl:null>",
                        postedDate = topic.postedDate,
                        content = topic.content
                    ),
                    replies = replies.map {
                        val replyAuthorSummary = summaries[it.authorId]
                        ReplyData(
                            replyId = it.replyId,
                            authorDisplayName = replyAuthorSummary?.displayName ?: "<replyAuthor.displayName:null>",
                            authorAvatarUrl = replyAuthorSummary?.avatarUrl ?: "<replyAuthor.avatarUrl:null>",
                            content = it.content,
                            postedDate = it.postedDate,
                            comments = it.comments.map { comment ->
                                val commentAuthorSummary = summaries[comment.authorId]
                                CommentData(
                                    commentId = comment.commentId,
                                    authorDisplayName = commentAuthorSummary?.displayName
                                        ?: "<commentAuthor.displayName:null>",
                                    authorAvatarUrl = commentAuthorSummary?.avatarUrl
                                        ?: "<commentAuthor.avatarUrl:null>",
                                    postedDate = comment.postedDate,
                                    content = comment.content
                                )
                            }
                        )
                    }
                )

                call.respond(ThymeleafContent("cafe/topicview", mapOf("data" to data)))
            }
        }

        post("/cafe/{section}/{id}/{title}") {
            guard(call, requireAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                val id = requireNotNull(call.request.pathVariables["id"])

                if (!sections.contains(section)) {
                    call.sectionNotFound()
                    return@guard
                }

                val topicId = serverContext.subunits.topic.getFullTopicId(id).okOrNull()
                if (topicId == null) {
                    call.topicNotFound()
                    return@guard
                }

                val replyPayload = JSON.decode<ReplyPayload>(call.receiveText())
                if (replyPayload.reply.length < 20) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Reply should be at least contains 20 characters."
                    )
                    return@guard
                }

                val replyId = Ids.uuid()
                val reply = Reply(
                    replyId = replyId,
                    topicId = topicId,
                    authorId = call.attributes.getAccount().userId,
                    content = replyPayload.reply,
                    postedDate = TimeCenter.now(),
                    comments = emptyList()
                )

                serverContext.subunits.reply.addReply(reply)
                    .onFail {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to reply")
                        return@guard
                    }

                Fancam.debug { "Created new replyId=$replyId" }
                call.respond(HttpStatusCode.OK)
            }
        }

        post("/cafe/{section}/{id}/{title}/{replyId}") {
            guard(call, requireAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                if (!sections.contains(section)) {
                    call.sectionNotFound()
                    return@guard
                }

                val replyId = requireNotNull(call.request.pathVariables["replyId"])
                val reply = serverContext.subunits.reply.getReply(replyId).okOrNull() ?: run {
                    call.respond(HttpStatusCode.NotFound, "reply not found")
                    return@guard
                }

                val commentPayload = JSON.decode<CommentPayload>(call.receiveText())
                if (commentPayload.comment.length < 10) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Comment should be at least contains 10 characters."
                    )
                    return@guard
                }

                val commentId = Ids.uuid()
                val comment = Comment(
                    commentId = commentId,
                    authorId = call.attributes.getAccount().userId,
                    content = commentPayload.comment,
                    postedDate = TimeCenter.now()
                )

                serverContext.subunits.reply.addComment(replyId, comment)
                    .onFail {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to post comment")
                        return@guard
                    }

                Fancam.debug { "Created new commentId=$commentId" }
                call.respond(HttpStatusCode.OK)
            }
        }

        post("/cafe/delete") {
            guard(call, requireAccountGuard) {
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
        }

        get("/cafe/{section}/write") {
            handle(call, requireAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                if (!sections.contains(section)) {
                    call.respond(HttpStatusCode.NotFound, "Section not found")
                    return@handle
                }

                call.respond(
                    ThymeleafContent(
                        "cafe/write",
                        mapOf(
                            "data" to WriteTopicModel(
                                account = call.attributes.getProfileAndMapToAccountModel(),
                                sectionName = requireNotNull(sections[section]) { "Ensure sections contains $section" }
                            )
                        )
                    )
                )
            }
        }

        post("/cafe/{section}/write") {
            handle(call, requireAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                if (!sections.contains(section)) {
                    call.respond(HttpStatusCode.NotFound, "Section not found")
                    return@handle
                }

                val post = JSON.decode<PostPayload>(call.receiveText())

                if (post.title.length < 8 || post.content.length < 20) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Title should be at least 8 characters and content should contains at least 20 characters"
                    )
                    return@handle
                }

                val acc = call.attributes.getAccount()

                val id = Ids.uuid()
                val topic = Topic(
                    topicId = id,
                    sectionId = section,
                    title = post.title,
                    authorId = acc.userId,
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
            handle(call, optionalAccountGuard) {
                call.respond(
                    ThymeleafContent(
                        "profile",
                        mapOf("data" to ProfileModel(call.attributes.getProfileAndMapToAccountModel()))
                    )
                )
            }
        }

        get("/login") {
            handle(call, mustNotHaveAccountGuard) {
                call.respond(
                    ThymeleafContent(
                        "login",
                        mapOf("data" to LoginModel(null))
                    )
                )
            }
        }

        get("/register") {
            handle(call, mustNotHaveAccountGuard) {
                call.respond(
                    ThymeleafContent(
                        "register",
                        mapOf("data" to RegisterModel(null))
                    )
                )
            }
        }

        get("/logout") {
            handle(call, optionalAccountGuard) {
                call.respond(
                    ThymeleafContent(
                        "logout", mapOf(
                            "data" to LogoutModel(
                                account = call.attributes.getProfileAndMapToAccountModel(),
                            )
                        )
                    )
                )
            }
        }
    }
}

fun <K, V> Map<K, V>.getOrDefaultAnd(key: K, value: V, action: () -> Unit) {

}

/**
 * Get [UserAccount] from the call's attributes.
 * This should only be called on `requireAccountGuard`.
 *
 * @throws IllegalArgumentException if not found.
 */
fun Attributes.getAccount(): UserAccount {
    return requireNotNull(getOrNull(SessionAccountKey)) {
        "Expected UserAccount but null"
    }
}

/**
 * Get [UserAccount] from the call's attributes.
 * @return `null` if not found.
 */
fun Attributes.getAccountOrNull(): UserAccount? {
    return getOrNull(SessionAccountKey)
}

fun Attributes.getProfileAndMapToAccountModel(): Account? {
    getOrNull(SessionAccountKey)?.let {
        return Account(
            username = it.username,
            level = it.profile.level
        )
    }
    return null
}

fun ResponseCookies.delete(name: String, path: String) {
    append(
        name = name,
        value = "",
        encoding = CookieEncoding.URI_ENCODING,
        maxAge = 0,
        expires = GMTDate(),
        domain = null,
        path = path
    )
}

suspend fun ApplicationCall.badRequest() {
    val data = ErrorModel(
        account = attributes.getProfileAndMapToAccountModel(),
        title = "Bad request",
        heading = "Bad request",
        message = "",
        action = Action("/", "Back to lobby")
    )
    respond(HttpStatusCode.BadRequest, ThymeleafContent("error", mapOf("data" to data)))
}

suspend fun ApplicationCall.sectionNotFound() {
    val data = ErrorModel(
        account = attributes.getProfileAndMapToAccountModel(),
        title = "Not Found",
        heading = "Section not found",
        message = "",
        action = Action("/", "Back to lobby")
    )
    respond(HttpStatusCode.NotFound, ThymeleafContent("error", mapOf("data" to data)))
}

suspend fun ApplicationCall.topicNotFound() {
    val data = ErrorModel(
        account = attributes.getProfileAndMapToAccountModel(),
        title = "Not Found",
        heading = "Topic not found",
        message = "",
        action = Action("/", "Back to lobby")
    )
    respond(HttpStatusCode.NotFound, ThymeleafContent("error", mapOf("data" to data)))
}

// represent account that is produced from session cookie
val SessionAccountKey = AttributeKey<UserAccount>("account")

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
            account = account?.let { Account(account.username, account.profile.level) },
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
        val usernameRegex = Regex("^[a-z0-9_]+$")

        post("/api/register") {
            handle(call, optionalAccountGuard) {
                if (call.attributes.getOrNull(SessionAccountKey) != null) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("reason" to "You are already logged in."))
                    return@handle
                }

                val data = JSON.decode<RegisterPayload>(call.receiveText())

                if (data.username.isBlank() || data.password.isBlank() || data.email.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("reason" to "blank credentials"))
                    return@handle
                }

                if (data.username.length < 2 || !usernameRegex.matches(data.username)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("reason" to "invalid username minimum 2 length, only (a-z, 0-9, _)")
                    )
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
                if (call.attributes.getOrNull(SessionAccountKey) != null) {
                    call.respondText("You are already logged in.")
                    return@handle
                }

                val data = JSON.decode<LoginPayload>(call.receiveText())

                if (data.username.isBlank() || data.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("reason" to "blank credentials"))
                    return@handle
                }

                if (data.username.length < 2 || !usernameRegex.matches(data.username)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("reason" to "invalid username minimum 2 length, only (a-z, 0-9, _)")
                    )
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

        post("/api/logout") {
            handle(call, NoAuthGuard) {
                val token = call.request.cookies["session"]
                if (token == null) {
                    call.respond(HttpStatusCode.Forbidden, "Not logged in")
                    return@handle
                }

                serverContext.subunits.websiteSession.delete(token)
                call.response.cookies.delete("session", "/")
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
