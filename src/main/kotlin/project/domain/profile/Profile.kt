package project.domain.profile

import kotlinx.serialization.Serializable

/**
 * User profile information.
 *
 * `Profile` contains the user's personal information and not including
 * system information, such as avatar, description, title, mood, etc.
 *
 * @property displayName Display name of the user.
 * @property avatarUrl Directory path that points to the user's avatar image.
 * @property level example field
 */
@Serializable
data class Profile(
    val displayName: String,
    val avatarUrl: String = "avatars/duck.jpg",
    val level: Int
)
