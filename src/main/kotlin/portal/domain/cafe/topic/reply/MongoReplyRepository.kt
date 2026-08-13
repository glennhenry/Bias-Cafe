package portal.domain.cafe.topic.reply

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.runMongoCatching
import encore.datastore.throwIfNothingMatched
import encore.utils.support.asUnit
import kotlinx.coroutines.flow.associate
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId
import portal.domain.cafe.topic.FieldTopicId

/** `topicId`*/
val FieldReplyId = Reply::replyId.name

/** `comments` */
val FieldComments = Reply::comments.name

class MongoReplyRepository(private val replies: MongoCollection<Reply>) : ReplyRepository {
    override suspend fun awaitInit() {}

    override suspend fun getReply(replyId: String): Result<Reply?> {
        return runMongoCatching {
            replies
                .find(Filters.eq(FieldReplyId, replyId))
                .firstOrNull()
        }
    }

    override suspend fun getRepliesUnder(topicId: String): Result<List<Reply>> {
        return runMongoCatching {
            replies
                .find(Filters.eq(FieldTopicId, topicId))
                .toList()
        }
    }

    override suspend fun getReplyCount(topicId: String): Result<Int?> {
        return runMongoCatching {
            replies
                .countDocuments(Filters.eq(FieldTopicId, topicId))
                .toInt()
        }
    }

    override suspend fun getReplyCounts(topicIds: List<String>): Result<Map<String, Int>> {
        return runMongoCatching {
            replies
                .withDocumentClass<ReplyCount>()
                .aggregate(
                    listOf(
                        Aggregates.match(Filters.`in`(FieldTopicId, topicIds)),
                        Aggregates.group("$$FieldTopicId", Accumulators.sum("count", 1)),
                        Aggregates.project(
                            Projections.fields(
                                Projections.excludeId(),
                                Projections.computed("topicId", $$"$_id"),
                                Projections.include("count")
                            )
                        )
                    )
                )
                .associate { it.topicId to it.count }
        }
    }

    override suspend fun addReply(reply: Reply): Result<Unit> {
        return runMongoCatching {
            replies.insertOne(reply).asUnit()
        }
    }

    override suspend fun getComments(replyId: String, limit: Int): Result<List<Comment>> {
        return runMongoCatching {
            replies
                .find(Filters.eq(FieldReplyId, replyId))
                .projection(
                    Projections.fields(
                        Projections.slice(FieldComments, limit),
                        Projections.excludeId()
                    )
                )
                .firstOrNull()
                ?.comments
        }
    }

    override suspend fun addComment(
        replyId: String,
        comment: Comment
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldReplyId, replyId)
            val update = Updates.addToSet(FieldComments, comment)

            replies.updateOne(filter, update)
                .throwIfNothingMatched("addComment", { filter })
        }
    }
}

data class ReplyCount(
    @field:BsonId val id: String? = null,
    val topicId: String,
    val count: Int
)
