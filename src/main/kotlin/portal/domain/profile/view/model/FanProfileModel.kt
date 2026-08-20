package portal.domain.profile.view.model

import portal.routes.common.AccountData

/**
 * The model for profile overview '/profile/@{username}/overview'
 *
 * @property account Optional [AccountData] of the user viewer.
 * @property displayName Display name of the profile.
 * @property avatarUrl Avatar URL of the profile.
 * @property startedStan Started stan of the profile.
 * @property favoriteSong Favorite song of the profile.
 * @property favoriteEra Favorite era of the profile.
 * @property bias Bias of the profile.
 * @property story Story of the profile.
 */
data class FanProfileModel(
    val account: AccountData?,
    val displayName: String,
    val avatarUrl: String,
    val startedStan: String,
    val favoriteSong: String,
    val favoriteEra: String,
    val bias: List<String>,
    val story: String
)
