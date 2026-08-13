package portal.domain.cafe.view.payload

import kotlinx.serialization.Serializable

/**
 * The payload sent by client from `topicview.html` for posting a reply.
 *
 * @property reply The content of the comment.
 */
@Serializable
data class ReplyPayload(
    val reply: String
)
