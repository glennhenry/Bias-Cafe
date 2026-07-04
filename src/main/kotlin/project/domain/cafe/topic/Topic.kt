package project.domain.cafe.topic

import kotlinx.serialization.Serializable
import project.domain.cafe.collection.Section

/**
 * Representation of cafe's topic in the database.
 *
 * @property topicId Unique identifier of the topic.
 * @property sectionId Refer to which [Section.id] is this topic posted in.
 * @property title The title of the topic.
 * @property author The author of the topic.
 * @property content The content of the topic.
 * @property postedDate Epoch millis of when the topic was posted.
 */
@Serializable
data class Topic(
    val topicId: String,
    val sectionId: String,
    val title: String,
    val author: String,
    val content: String,
    val postedDate: Long
)