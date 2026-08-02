package project.domain.cafe.topic

import kotlinx.serialization.Serializable
import project.domain.cafe.collection.Section
import project.mongo.collection.UserAccount

/**
 * Representation of cafe's topic in the database.
 *
 * @property topicId Unique identifier of the topic.
 * @property sectionId Identifier of the section this topic belongs to. References [Section.id].
 * @property title The title of the topic.
 * @property authorId Identifier of the user who posted this topic. References [UserAccount.userId].
 * @property content The content of the topic.
 * @property postedDate Epoch millis of when the topic was posted.
 */
@Serializable
data class Topic(
    val topicId: String,
    val sectionId: String,
    val title: String,
    val authorId: String,
    val content: String,
    val postedDate: Long
)
