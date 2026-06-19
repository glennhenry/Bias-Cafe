package encore.datastore.collection

import kotlinx.serialization.Serializable

/**
 * User profile information.
 *
 * `Profile` contains the user's personal information and not including
 * system information, such as avatar, description, title, mood, etc.
 *
 * @property userId Unique identifier of the user.
 */
@Serializable
data class Profile(
    val userId: UserId,
)