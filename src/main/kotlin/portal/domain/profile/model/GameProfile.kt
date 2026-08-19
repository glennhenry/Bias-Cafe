package portal.domain.profile.model

import kotlinx.serialization.Serializable

/**
 * Profile data specific to the gamification of the platform.
 * It contains various game-like data of the user.
 *
 * @property level The level of user.
 * @property coins Amount of currency the user has.
 * @property badges Badges of user.
 * @property achievements Achievements of user.
 */
@Serializable
data class GameProfile(
    val level: UserLevel,
    val coins: Int,
    val badges: List<String>,
    val achievements: List<String>
)

/**
 * Representation of level.
 *
 * @property currentLevel The current level number.
 * @property xp The current experience points.
 */
@Serializable
data class UserLevel(
    val currentLevel: Int,
    val xp: Int
)
