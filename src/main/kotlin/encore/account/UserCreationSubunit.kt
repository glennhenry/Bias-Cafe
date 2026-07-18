package encore.account

import encore.account.model.UserMetadata
import encore.datastore.BlankDataStore
import encore.datastore.DataStore
import encore.datastore.collection.ServerObjects
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.fancam.Fancam
import encore.fancam.Tags
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.time.TimeCenter
import encore.utils.hash
import encore.utils.identifier.Ids
import project.domain.profile.Profile

/**
 * Server-scoped subunit responsible for user creation.
 *
 * Responsible for handling the logic to create a user, which involves
 * inserting some default data to the base collections: [UserAccount].
 *
 * This subunit doesn't handle server data update in [ServerObjects]
 * for the user. This should be handled separately via external orchestration
 * (e.g., in the account registration).
 *
 * @property dataStore [DataStore] to persist the newly created users.
 */
class UserCreationSubunit(
    private val dataStore: DataStore
) : Subunit<ServerScope> {
    /**
     * Create a user account with the specified [username], [password], and [email].
     *
     * Email is optional and will be defaulted to `username@email.com`
     *
     * @return [UserId] of the newly created user
     * @throws [Throwable] an exception type from the underlying datastore or
     *         [IllegalStateException] when the account creation failed without any exception passed.
     */
    suspend fun createUser(
        username: String, password: String,
        email: String = "$username@email.com"
    ): UserId {
        val userId = Ids.uuid()

        val now = TimeCenter.now()
        val account = UserAccount(
            userId = userId,
            username = username,
            email = email,
            hashedPassword = hash(password),
            registeredAt = now,
            lastActiveAt = now,
            metadata = UserMetadata(),
            profile = Profile(
                displayName = username,
                avatarUrl = "",
                level = 1
            )
        )

        val result = dataStore.create(account)
        if (result.isSuccess) {
            return userId
        }

        Fancam.error(tag = Tags.Creation) { "Account creation failed for $username" }

        throw result.exceptionOrNull()
            ?: IllegalStateException("Account creation failed with unknown scandal (exception was null)")
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> = Result.success(Unit)
    override suspend fun disband(scope: ServerScope): Result<Unit> = Result.success(Unit)

    companion object {
        /**
         * Creates a test instance of [UserCreationSubunit].
         *
         * @param dataStore dependency for persistence.
         * Use [BlankDataStore] when the behavior is not relevant to the test.
         */
        fun createForTest(
            dataStore: DataStore = BlankDataStore()
        ): UserCreationSubunit {
            return UserCreationSubunit(dataStore)
        }
    }
}
