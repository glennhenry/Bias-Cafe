package portal.domain.cafe.view.payload

import kotlinx.serialization.Serializable

/**
 * The payload sent by client from `writetopic.html` for posting a new topic.
 *
 * @property title
 * @property content
 */
@Serializable
data class WriteTopicPayload(
    val title: String,
    val content: String
)
