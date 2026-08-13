package portal

import encore.creation.UserCreationFactory
import encore.time.TimeCenter
import encore.utils.hash
import encore.utils.identifier.Ids
import encore.utils.types.Report
import portal.domain.profile.Profile
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
            email = email,
            hashedPassword = hash(password),
            registeredAt = now,
            lastActiveAt = now,
            extra = emptyMap(),
            profile = Profile(
                displayName = username,
                level = 1
            )
        )
        return account
    }

    override fun updateServerObjects(account: UserAccount): Report {
        // update server objects if needed...
        return Report.Ok
    }
}
