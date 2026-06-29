package encore.datastore.collection

import kotlinx.serialization.Serializable

/**
 * Representation of cafe's topic in the database.
 *
 * @property topicId Unique identifier of the topic.
 * @property title The title of the topic.
 * @property author The author of the topic.
 * @property content The content of the topic.
 * @property postedAt Epoch millis of when the topic was posted.
 */
@Serializable
data class Topic(
    val topicId: String,
    val title: String,
    val author: String,
    val content: String,
    val postedAt: Long
)
