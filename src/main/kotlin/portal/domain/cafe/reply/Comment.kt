package portal.domain.cafe.reply

import kotlinx.serialization.Serializable
import portal.mongo.collection.UserAccount

/**
 * Represent a direct respond to a [Reply].
 *
 * @property commentId Unique identifier of the comment.
 * @property authorId Identifier of the user who posted this reply. References [UserAccount.userId].
 * @property content The content of the comment.
 * @property postedDate Epoch millis of when the comment was posted.
 */
@Serializable
data class Comment(
    val commentId: String,
    val authorId: String,
    val content: String,
    val postedDate: Long
)
