package portal.domain.profile

import kotlinx.serialization.Serializable
import portal.mongo.collection.UserAccount

/**
 * Represent a summarized view of a user account and profile.
 *
 * This model is a small data typically used across the website
 * that doesn't include every fields of [UserAccount] and [Profile]
 *
 * @property userId References to [UserAccount].
 * @property displayName Display name of the user.
 * @property avatarUrl Directory path that points to the user's avatar image.
 */
@Serializable
data class UserSummary(
    val userId: String,
    val displayName: String,
    val avatarUrl: String
)
