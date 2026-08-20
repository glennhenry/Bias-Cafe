package portal.domain.dummy

import encore.creation.UserCreationFactory
import encore.creation.UserCreationSubunit
import encore.utils.types.Report
import portal.RealUserCreationFactory
import portal.domain.profile.model.Profile
import portal.mongo.collection.UserAccount
import portal.mongo.collection.UserId

/**
 * Fake implementation of [UserCreationFactory] where data generation
 * is supplied directly from the caller via the input map on the constructor.
 *
 * It allows caller to create account and profile, while the actual invocation
 * done by [UserCreationSubunit] to this factory with [UserCreationSubunit.createUser]
 * is solely a lookup and returning the corresponding accounts or profiles key-ed
 * by the [UserId]. **This means the passed username, email, or password is ignored.**
 *
 * This is typically used when caller want to opt-in a specific configuration for
 * user profiles or wants to generate dummy accounts, rather than relying on
 * [RealUserCreationFactory].
 *
 * It is the caller's responsibility to ensure that [UserAccount.userId] and
 * [Profile.userId] or other duplicate fields matches.
 *
 * The absence of certain `userId` on [accounts] map or [profiles] will
 * throw an [IllegalStateException].
 *
 * @param accounts Map of [UserId] to each [UserAccount], used in [account].
 * @param profiles Map of [UserId] to each [Profile], used in [profile].
 * @param updateServerObject Lambda to be invoked for [updateServerObjects]
 */
class FakeCreationFactory(
    private val nextUserId: () -> UserId,
    private val accounts: MutableMap<UserId, UserAccount>,
    private val profiles: MutableMap<UserId, Profile>,
    private val updateServerObject: (UserAccount) -> Report
) : UserCreationFactory {
    override fun userId(isAdmin: Boolean): UserId {
        return nextUserId()
    }

    override fun account(
        userId: UserId,
        username: String,
        password: String,
        email: String
    ): UserAccount {
        return accounts[userId] ?: error("userId=$userId is not found on accounts map.")
    }

    override fun profile(userId: UserId, username: String): Profile {
        return profiles[userId] ?: error("userId=$userId is not found on profiles map.")
    }

    override fun updateServerObjects(account: UserAccount): Report = updateServerObject(account)
}
