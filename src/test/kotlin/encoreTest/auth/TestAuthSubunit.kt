package encoreTest.auth

import TestMongoCollectionName
import encore.account.AccountRepository
import encore.account.AccountSubunit
import encore.account.MongoAccountRepository
import encore.account.UserCreationSubunit
import encore.account.model.Credentials
import encore.account.model.UserMetadata
import encore.auth.AuthSubunit
import encore.auth.LoginResult
import encore.datastore.MongoDataStore
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.session.SessionSubunit
import encore.time.source.SystemTimeSource
import encore.utils.types.Outcome
import encore.utils.types.isFail
import encore.utils.types.isOk
import encore.utils.types.okOrThrow
import initMongo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration test for [AuthSubunit] and [AccountRepository].
 *
 * Ensure MongoDB is running.
 *
 * [AuthSubunit.isUsernameAvailable] has extra logic than [AccountRepository.usernameExists]
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
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs)

        val account = UserAccount(
            userId = "pid12345",
            username = "name",
            email = "anyemail",
            hashedPassword = "anypassword",
            registeredAt = 0,
            lastActiveAt = 0,
            metadata = UserMetadata(),
        )
        collection.insertOne(account)

        assertFalse(auth.isUsernameAvailable("name").okOrThrow())
        assertTrue(repo.usernameExists("name").getOrThrow())
    }

    @Test
    fun `username should be available if user is not registered`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs)

        assertTrue(auth.isUsernameAvailable("xyz").okOrThrow())
        assertFalse(repo.usernameExists("xyz").getOrThrow())
    }

    @Test
    fun `register should create user`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs)

        auth.register("helloworld", "kotlinktor", "helloworld@email.com")
        assertFalse(auth.isUsernameAvailable("helloworld").okOrThrow())
        assertTrue(repo.usernameExists("helloworld").getOrThrow())
    }

    @Test
    fun `register failed because username or email is duplicate`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs)

        auth.register("helloworld1", "kotlinktor", "helloworld1@email.com")
        // duplicate username fail
        val res1 = auth.register("helloworld1", "kotlinktor", "helloworld2@email.com")
        assertTrue(res1.isFail())

        auth.register("worldhello1", "kotlinktor", "worldhello1@email.com")
        // duplicate email fail
        val res2 = auth.register("worldhello2", "kotlinktor", "worldhello1@email.com")
        assertTrue(res2.isFail())
    }

    @Test
    fun `login failures when account don't exist`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

        val db = MongoDataStore(mongoDb, TestMongoCollectionName)
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs)

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
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs)

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
        val repo = object : AccountRepository {
            override suspend fun getAccountByUserId(userId: String): Result<UserAccount?> = TODO()
            override suspend fun getAccountByUsername(username: String): Result<UserAccount?> = TODO()
            override suspend fun getUserIdByUsername(username: String): Result<UserId?> = TODO()
            override suspend fun getCredentials(username: String): Result<Credentials?> =
                Result.failure(RuntimeException("xiaoting"))
            override suspend fun updateUserAccount(userId: UserId, account: UserAccount): Result<Unit> = TODO()
            override suspend fun updateLastActivity(userId: UserId, lastActivity: Long): Result<Unit> = TODO()
            override suspend fun usernameExists(username: String): Result<Boolean> = TODO()
            override suspend fun emailExists(email: String): Result<Boolean> = TODO()
        }
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs)

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
        val repo = MongoAccountRepository(collection)
        val accountSubunit = AccountSubunit(repo)
        val pcs = UserCreationSubunit(db)
        val auth = AuthSubunit(accountSubunit, pcs)

        auth.register("helloworld", "kotlinktor", "helloworld@email.com")
        val session = auth.login("helloworld", "kotlinktor")
        assertTrue(((session as Outcome.Ok).value is LoginResult.Success))
    }
}
