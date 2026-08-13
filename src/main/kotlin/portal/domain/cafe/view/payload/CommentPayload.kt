package portal.domain.cafe.view.payload

import kotlinx.serialization.Serializable

/**
 * The payload sent by client from `topicview.html` for posting comments.
 *
 * @property comment The content of the comment.
 */
@Serializable
data class CommentPayload(
    val comment: String
)
