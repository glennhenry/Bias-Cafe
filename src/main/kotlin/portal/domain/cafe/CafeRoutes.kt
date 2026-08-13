package portal.domain.cafe

import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard
import encore.route.handle
import encore.serialization.JSON
import encore.time.TimeCenter
import encore.utils.identifier.Ids
import encore.utils.identifier.shortUuid
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
import portal.context.ServerContext
import portal.domain.cafe.reply.Comment
import portal.domain.cafe.reply.Reply
import portal.domain.cafe.topic.Topic
import portal.domain.cafe.topic.TopicDeletionOutcome
import portal.domain.cafe.view.model.*
import portal.domain.cafe.view.payload.CommentPayload
import portal.domain.cafe.view.payload.ReplyPayload
import portal.domain.cafe.view.payload.WriteTopicPayload
import portal.routes.common.Action
import portal.routes.common.ErrorModel
import portal.routes.guard.OptionalAccountGuard
import portal.routes.guard.RequireAccountGuard
import portal.routes.guard.getAccountData
import portal.routes.guard.getUserAccount

val Sections = mapOf(
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

class CafeRoutes(private val serverContext: ServerContext) : RouteHandler {
    private val optionalAccountGuard = OptionalAccountGuard(serverContext)
    private val requireAccountGuard = RequireAccountGuard(serverContext)

    override fun Route.install() {
        get("/cafe") {
            guard(call, optionalAccountGuard) {
                val spaces = serverContext.subunits.collection.getSpacesForLandingModel()
                val counts = serverContext.subunits.topic.getTopicsCountForEachSection().okOrThrow()

                val data = CafeModel(
                    account = call.attributes.getAccountData(),
                    spaces = spaces,
                    topicCounts = counts
                )

                call.respond(ThymeleafContent("cafe/cafe", mapOf("data" to data)))
            }
        }

        get("/cafe/{section}") {
            guard(call, optionalAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])

                if (!Sections.contains(section)) {
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
                    account = call.attributes.getAccountData(),
                    sectionId = section,
                    topics = topics.map {
                        TopicListItemData(
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

        get("/cafe/{section}/write") {
            handle(call, requireAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                if (!Sections.contains(section)) {
                    call.respond(HttpStatusCode.NotFound, "Section not found")
                    return@handle
                }

                call.respond(
                    ThymeleafContent(
                        "cafe/writetopic",
                        mapOf(
                            "data" to WriteTopicModel(
                                account = call.attributes.getAccountData(),
                                sectionName = requireNotNull(Sections[section]) { "Ensure Sections contains $section" }
                            )
                        )
                    )
                )
            }
        }

        post("/cafe/{section}/write") {
            handle(call, requireAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                if (!Sections.contains(section)) {
                    call.respond(HttpStatusCode.NotFound, "Section not found")
                    return@handle
                }

                val post = JSON.decode<WriteTopicPayload>(call.receiveText())

                if (post.title.length < 8 || post.content.length < 20) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Title should be at least 8 characters and content should contains at least 20 characters"
                    )
                    return@handle
                }

                val acc = call.attributes.getUserAccount()

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

        get("/cafe/{section}/{id}/{title}") {
            guard(call, optionalAccountGuard) {
                val section = requireNotNull(call.request.pathVariables["section"])
                val id = requireNotNull(call.request.pathVariables["id"])
                val title = requireNotNull(call.request.pathVariables["title"])

                if (!Sections.contains(section)) {
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
                    account = call.attributes.getAccountData(),
                    sectionName = requireNotNull(Sections[section]) { "Ensure Sections contains $section" },
                    topicId = topic.topicId,
                    topic = TopicViewData(
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

                if (!Sections.contains(section)) {
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
                    authorId = call.attributes.getUserAccount().userId,
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
                if (!Sections.contains(section)) {
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
                    authorId = call.attributes.getUserAccount().userId,
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
    }
}

/**
 * Respond with an error page when the requested section is not found.
 */
suspend fun ApplicationCall.sectionNotFound() {
    val data = ErrorModel(
        account = attributes.getAccountData(),
        title = "Not Found",
        heading = "Section not found",
        message = "",
        action = Action("/", "Back to lobby")
    )
    respond(HttpStatusCode.NotFound, ThymeleafContent("error", mapOf("data" to data)))
}

/**
 * Respond with an error page when the requested topic is not found.
 */
suspend fun ApplicationCall.topicNotFound() {
    val data = ErrorModel(
        account = attributes.getAccountData(),
        title = "Not Found",
        heading = "Topic not found",
        message = "",
        action = Action("/", "Back to lobby")
    )
    respond(HttpStatusCode.NotFound, ThymeleafContent("error", mapOf("data" to data)))
}
