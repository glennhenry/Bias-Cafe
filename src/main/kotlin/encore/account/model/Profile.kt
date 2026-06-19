package encore.account.model

import encore.datastore.collection.UserId
import kotlinx.serialization.Serializable

/**
 * User profile information.
 *
 * `Profile` contains the user's personal information in the server
 * such as country, avatar, locale, etc. It does not include game-specific
 * information like user's ranking or status.
 *
 * @property userId Unique identifier of the user.
 * @property createdAt Epoch millis of the account creation date.
 * @property lastActiveAt Epoch millis of the account last activity.
 */
@Serializable
data class Profile(
    val userId: UserId,
    val createdAt: Long,
    val lastActiveAt: Long,
)
