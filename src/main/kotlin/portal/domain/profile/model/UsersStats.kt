package portal.domain.profile.model

import kotlinx.serialization.Serializable

/**
 * Users statistics information in the platform.
 * This is typically just a denormalized data, summarized view from the main collection.
 *
 * @property numTopics Number of topic user has posted.
 * @property numReplies Number of replies user has posted.
 */
@Serializable
data class UsersStats(
    val numTopics: Int,
    val numReplies: Int
)
