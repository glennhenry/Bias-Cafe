package project.domain.cafe.topic.reply

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.runMongoCatching
import encore.utils.support.asUnit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import project.domain.cafe.topic.FieldTopicId

/** `topicId`*/
val FieldReplyId = Reply::replyId.name

class MongoReplyRepository(private val replies: MongoCollection<Reply>) : ReplyRepository {
    override suspend fun awaitInit() {
        replies.createIndex(Indexes.text("topicId"))
        replies.createIndex(Indexes.text("replyId"))
    }

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

    override suspend fun addReply(reply: Reply): Result<Unit> {
        return runMongoCatching {
            replies.insertOne(reply).asUnit()
        }
    }
}
