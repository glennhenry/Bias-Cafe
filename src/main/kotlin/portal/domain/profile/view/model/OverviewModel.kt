package portal.domain.profile.view.model

import portal.routes.common.AccountData

/**
 * The model for profile overview '/profile/@{username}/overview'
 *
 * @property account Optional [AccountData] of the user viewer.
 * @property username Username of the profile.
 * @property displayName Display name of the profile.
 * @property avatarUrl Avatar URL of the profile.
 * @property country Country of the profile.
 * @property birthday Birthday string of the profile.
 * @property bio Bio of the profile.
 */
data class OverviewModel(
    val account: AccountData?,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val country: String,
    val birthday: String,
    val bio: String,
)
