package encore.account

import encore.account.model.UserMetadata
import encore.datastore.collection.Profile
import encore.datastore.BlankDataStore
import encore.datastore.DataStore
import encore.datastore.collection.*
import encore.fancam.Fancam
import encore.fancam.Tags
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.time.TimeCenter
import encore.utils.identifier.Ids
import encore.utils.hash
import project.Globals

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
class UserCreationSubunit(private val dataStore: DataStore) : Subunit<ServerScope> {
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
        )
        val profile = Profile(userId)

        val result = dataStore.create(account, profile)
        if (result.isSuccess) {
            return userId
        }

        Fancam.error(tag = Tags.Creation) { "Account creation failed for $username" }

        throw result.exceptionOrNull()
            ?: IllegalStateException("Account creation failed with unknown scandal (exception was null)")
    }

    /**
     * Create a reserved admin account if it doesn't exist.
     *
     * @param alwaysRecreate Whether to always recreate the account.
     * @throws [Throwable] an exception type from the underlying datastore or
     *         [IllegalStateException] when the account creation failed without any exception passed.
     */
    suspend fun createAdmin(adminData: Globals, alwaysRecreate: Boolean = false) {
        if (alwaysRecreate) {
            dataStore.delete(adminData.ADMIN_PLAYER_ID)
        } else if (dataStore.userExists(adminData.ADMIN_PLAYER_ID)) {
            Fancam.info(Tags.Creation) { "Ignoring admin account creation (already exists)" }
            return
        }

        val now = TimeCenter.now()
        val account = UserAccount(
            userId = Globals.ADMIN_PLAYER_ID,
            username = Globals.ADMIN_USERNAME,
            email = Globals.ADMIN_EMAIL,
            hashedPassword = Globals.ADMIN_HASHED_PASSWORD,
            registeredAt = now,
            lastActiveAt = now,
            metadata = UserMetadata(),
        )
        val profile = Profile(Globals.ADMIN_PLAYER_ID)

        val result = dataStore.create(account, profile)

        if (result.isSuccess) {
            Fancam.info(Tags.Creation) { "New admin account created with username=${Globals.ADMIN_USERNAME}, userId=${Globals.ADMIN_PLAYER_ID}" }
        } else {
            Fancam.error(tag = Tags.Creation) { "Admin account creation failed" }

            throw result.exceptionOrNull()
                ?: IllegalStateException("Admin account creation failed with unknown scandal (exception was null)")
        }
    }

    private fun defaultProfile(userId: UserId): Profile {
        return Profile(
            userId = userId
        )
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
        fun createForTest(dataStore: DataStore = BlankDataStore()): UserCreationSubunit {
            return UserCreationSubunit(dataStore)
        }
    }
}
