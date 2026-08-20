package portal.domain.profile.model

import kotlinx.serialization.Serializable

/**
 * Contains only the necessary profile information for the profile overview page.
 * It's basically the root fields of [Profile].
 *
 * @property displayName Display name of the user, non-unique.
 * @property avatarUrl Directory path that points to the user's avatar image.
 * @property country The country origin of user.
 * @property birthday The day when the user was born (or whatever they claim 😅).
 * @property bio Self-description of user about themselves.
 */
@Serializable
data class OverviewSummary(
    val displayName: String,
    val avatarUrl: String,
    val country: String,
    val birthday: String,
    val bio: String,
)
