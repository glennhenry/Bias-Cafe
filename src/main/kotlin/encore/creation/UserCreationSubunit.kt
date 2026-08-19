package encore.creation

import encore.datastore.BlankDataStore
import encore.datastore.DataStore
import encore.fancam.Fancam
import encore.fancam.Tags
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.utils.types.isOk
import portal.domain.profile.subunits.BlankProfileRepository
import portal.domain.profile.subunits.ProfileRepository
import portal.mongo.collection.UserId

/**
 * Server-scoped subunit responsible for user creation.
 *
 * Responsible for orchestrating the creation of a new user.
 * It consults [UserCreationFactory] for producing new documents
 * and updating existing server data collection.
 *
 * @property dataStore [DataStore] implementation for inserting new documents.
 * @property factory [UserCreationFactory] producing documents and updating server collection.
 */
class UserCreationSubunit(
    private val dataStore: DataStore,
    private val profileRepository: ProfileRepository,
    private val factory: UserCreationFactory
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
        val userId = factory.userId(false)
        val account = factory.account(userId, username, password, email)
        val profile = factory.profile(userId, username)

        val accountInsertResult = dataStore.insert(account)
        val profileInsertResult = profileRepository.insert(profile)
        val serverObjReport = factory.updateServerObjects(account)

        if (accountInsertResult.isSuccess &&
            profileInsertResult.isSuccess &&
            serverObjReport.isOk()
        ) {
            return userId
        }

        Fancam.error(tag = Tags.Creation) {
            "Account creation failed for $username (" +
                    "accountInsertResult.isSuccess=${accountInsertResult.isSuccess}, " +
                    "profileInsertResult.isSuccess=${profileInsertResult.isSuccess}, " +
                    "serverObjReport.isOk=${serverObjReport.isOk()})"
        }

        throw IllegalStateException("Account creation failed with unknown scandal (exception was null)")
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> = Result.success(Unit)
    override suspend fun disband(scope: ServerScope): Result<Unit> = Result.success(Unit)

    companion object {
        /**
         * Creates a test instance of [UserCreationSubunit].
         *
         * @param dataStore dependency for persistence.
         * Use [BlankDataStore] when the behavior is not relevant to the test.
         * @param factory [UserCreationFactory].
         */
        fun createForTest(
            dataStore: DataStore = BlankDataStore(),
            profileRepository: ProfileRepository = BlankProfileRepository(),
            factory: UserCreationFactory = BlankUserCreationFactory()
        ): UserCreationSubunit {
            return UserCreationSubunit(dataStore, profileRepository, factory)
        }
    }
}
