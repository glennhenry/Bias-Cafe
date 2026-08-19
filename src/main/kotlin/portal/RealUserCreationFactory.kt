package portal

import encore.creation.UserCreationFactory
import encore.time.TimeCenter
import encore.utils.hash
import encore.utils.identifier.Ids
import encore.utils.types.Report
import portal.domain.profile.model.FanProfile
import portal.domain.profile.model.GameProfile
import portal.domain.profile.model.Profile
import portal.domain.profile.model.UserLevel
import portal.domain.profile.model.UsersStats
import portal.mongo.collection.UserAccount
import portal.mongo.collection.UserId

/**
 * Concrete implementation of [UserCreationFactory] that must be
 * updated overtime. Add server object repositories as dependency if needed.
 */
class RealUserCreationFactory : UserCreationFactory {
    override fun userId(isAdmin: Boolean): UserId {
        if (isAdmin) return Globals.ADMIN_PLAYER_ID
        return Ids.uuid()
    }

    override fun account(
        userId: UserId,
        username: String,
        password: String,
        email: String
    ): UserAccount {
        val now = TimeCenter.now()
        val account = UserAccount(
            userId = userId,
            username = username,
            displayName = username,
            email = email,
            hashedPassword = hash(password),
            registeredAt = now,
            lastActiveAt = now,
            extra = emptyMap(),
        )
        return account
    }

    override fun profile(userId: UserId, username: String): Profile {
        return Profile(
            userId = userId,
            displayName = username,
            // avatarUrl = ,
            country = "",
            birthday = "",
            bio = "This user is too lazy to write anything here",
            fanProfile = FanProfile(
                bias = emptyList(),
                favoriteSong = "",
                favoriteEra = "",
                story = "Write your story about Kep1er!"
            ),
            gameProfile = GameProfile(
                level = UserLevel(1, 0),
                coins = 100,
                badges = emptyList(),
                achievements = emptyList()
            ),
            blockedUsers = emptyList(),
            stats = UsersStats(
                numTopics = 0,
                numReplies = 0
            )
        )
    }

    override fun updateServerObjects(account: UserAccount): Report {
        // update server objects if needed...
        return Report.Ok
    }
}
