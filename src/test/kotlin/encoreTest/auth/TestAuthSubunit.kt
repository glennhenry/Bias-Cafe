package encoreTest.auth

import TestMongoCollectionName
import com.mongodb.assertions.Assertions
import encore.account.AccountRepository
import encore.account.AccountSubunit
import encore.account.MongoAccountRepository
import encore.account.UserCreationSubunit
import encore.account.model.Credentials
import encore.account.model.Profile
import encore.auth.AuthSubunit
import encore.auth.LoginResult
import encore.datastore.MongoDataStore
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.session.SessionSubunit
import encore.time.source.SystemTimeSource
import encore.utils.types.Outcome
import encore.utils.types.okOrThrow
import initMongo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import testUtils.createProfile
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Integration test for [encore.auth.AuthSubunit] and [encore.account.AccountRepository].
 *
 * Ensure MongoDB is running.
 *
 * [encore.auth.AuthSubunit.isUsernameAvailable] has extra logic than [encore.account.AccountRepository.usernameExists]
 * try calling `repo.usernameExists("name").getOrThrow()` in addition to `isUsernameAvailable`.
 */
class TestAuthSubunit {
    private fun scope(): CoroutineScope {
        return TestScope(StandardTestDispatcher())
    }

    @Test
    fun `username shouldn't be available after user is registered`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val manager = SessionSubunit(scope(), SystemTimeSource())
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs, manager)

        val account = UserAccount(
            userId = "pid12345",
            username = "name",
            email = "anyemail",
            hashedPassword = "anypassword",
            profile = createProfile("pid12345")
        )
        collection.insertOne(account)

        Assertions.assertFalse(auth.isUsernameAvailable("name").okOrThrow())
        assertTrue(repo.usernameExists("name").getOrThrow())
    }

    @Test
    fun `username should be available if user is not registered`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val manager = SessionSubunit(scope(), SystemTimeSource())
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs, manager)

        assertTrue(auth.isUsernameAvailable("xyz").okOrThrow())
        Assertions.assertFalse(repo.usernameExists("xyz").getOrThrow())
    }

    @Test
    fun `register should create user`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val manager = SessionSubunit(scope(), SystemTimeSource())
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs, manager)

        auth.register("helloworld", "kotlinktor", "helloworld@email.com")
        Assertions.assertFalse(auth.isUsernameAvailable("helloworld").okOrThrow())
        assertTrue(repo.usernameExists("helloworld").getOrThrow())
    }

    @Test
    fun `login failures when account don't exist`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val manager = SessionSubunit(scope(), SystemTimeSource())
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs, manager)

        val session = auth.login("asdf", "fdsa")
        // Ok = no internal error
        assertTrue((session as Outcome.Ok).value is LoginResult.AccountNotFound)
    }

    @Test
    fun `login failures when credentials are wrong`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val manager = SessionSubunit(scope(), SystemTimeSource())
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs, manager)

        auth.register("helloworld", "kotlinktor", "helloworld@email.com")
        val session = auth.login("helloworld", "ktor")
        assertTrue((session as Outcome.Ok).value is LoginResult.InvalidCredentials)
    }

    @Test
    fun `login failures when repository has internal error`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val manager = SessionSubunit(scope(), SystemTimeSource())
        val repo = object : AccountRepository {
            override suspend fun getAccountByUsername(username: String): Result<UserAccount?> = TODO()
            override suspend fun getUserIdByUsername(username: String): Result<UserId?> = TODO()
            override suspend fun getCredentials(username: String): Result<Credentials?> =
                Result.failure(RuntimeException("xiaoting"))
            override suspend fun getProfile(userId: UserId): Result<Profile?> = TODO()
            override suspend fun updateUserAccount(userId: UserId, account: UserAccount): Result<Unit> = TODO()
            override suspend fun updateProfile(userId: UserId, profile: Profile): Result<Unit> = TODO()
            override suspend fun updateLastActivity(userId: UserId, lastActivity: Long): Result<Unit> = TODO()
            override suspend fun usernameExists(username: String): Result<Boolean> = TODO()
            override suspend fun emailExists(email: String): Result<Boolean> = TODO()
        }
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs, manager)

        auth.register("helloworld", "kotlinktor", "helloworld@email.com")
        val session = auth.login("helloworld", "ktor")
        // should error on first call to repository in getCredentials
        assertTrue(session is Outcome.Fail)
    }

    @Test
    fun `login success when user is registered and credentials are correct`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val manager = SessionSubunit(scope(), SystemTimeSource())
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs, manager)

        auth.register("helloworld", "kotlinktor", "helloworld@email.com")
        val session = auth.login("helloworld", "kotlinktor")
        assertTrue(((session as Outcome.Ok).value is LoginResult.Success))
    }
}