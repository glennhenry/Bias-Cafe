package portal.domain.profile.model

import kotlinx.serialization.Serializable

/**
 * Profile data related to the one's identity being a fan.
 *
 * @property startedStan Date when the profile started being a stan.
 * @property favoriteSong The favorite song.
 * @property favoriteEra The favorite era.
 * @property bias The list of biased members.
 * @property story A text description on user's story about the group.
 */
@Serializable
data class FanProfile(
    val startedStan: String,
    val favoriteSong: String,
    val favoriteEra: String,
    val bias: List<String>,
    val story: String
)
