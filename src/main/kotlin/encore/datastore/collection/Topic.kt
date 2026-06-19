package encore.datastore.collection

import kotlinx.serialization.Serializable

/**
 * Representation of cafe's topic in the database.
 *
 * @property topicId Unique identifier of the topic.
 * @property content The content of this topic.
 */
@Serializable
data class Topic(
    val topicId: String,
    val content: String
)
