package portal.domain.profile.model

import kotlinx.serialization.Serializable

/**
 * Contains only the necessary fan profile information for the fan profile page.
 * It's basically the [Profile.fanProfile] with `displayName` and `avatarUrl`.
 *
 * @property displayName Display name of the user, non-unique.
 * @property avatarUrl Directory path that points to the user's avatar image.
 * @property startedStan Started stanning date.
 * @property favoriteSong The favorite song.
 * @property favoriteEra The favorite era.
 * @property bias The list of biased members.
 * @property story A text description on user's story about the group.
 */
@Serializable
data class FanProfileSummary(
    val displayName: String,
    val avatarUrl: String,
    val startedStan: String,
    val favoriteSong: String,
    val favoriteEra: String,
    val bias: List<String>,
    val story: String
)
