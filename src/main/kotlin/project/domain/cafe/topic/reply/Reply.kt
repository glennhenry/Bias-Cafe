package project.domain.cafe.topic.reply

import kotlinx.serialization.Serializable
import project.domain.cafe.topic.Topic
import project.mongo.collection.UserAccount

/**
 * Representation of cafe's topic reply in the database.
 *
 * @property replyId Unique identifier of the reply.
 * @property topicId Identifier of the topic this reply belongs to. References [Topic.topicId].
 * @property authorId Identifier of the user who posted this reply. References [UserAccount.userId].
 * @property content The content of the reply.
 * @property postedDate Epoch millis of when the reply was posted.
 * @property comments [Comment]s for this reply.
 */
@Serializable
data class Reply(
    val replyId: String,
    val topicId: String,
    val authorId: String,
    val content: String,
    val postedDate: Long,
    val comments: List<Comment>
)
