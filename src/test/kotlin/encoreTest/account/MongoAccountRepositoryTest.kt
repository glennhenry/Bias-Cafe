package encoreTest.account

import TestCollections
import encore.account.Credentials
import encore.account.MongoAccountRepository
import encore.utils.hash
import initMongo
import kotlinx.coroutines.test.runTest
import project.mongo.collection.UserAccount
import testUtils.createAccount
import testUtils.createProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test operations of [MongoAccountRepository].
 */
class MongoAccountRepositoryTest {
    @Test
    fun `test all`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestCollections.userAccount)
        collection.drop()
        mongoDb.createCollection(TestCollections.userAccount)

        val repo = MongoAccountRepository(collection)

        val id = "id123"
        val name = "name123"
        val email = "name@email.com"
        val account = UserAccount(
            id,
            name,
            email,
            hash("pw123"),
            registeredAt = 1,
            lastActiveAt = 1,
            extra = emptyMap(),
            profile = createProfile()
        )

        collection.insertMany(List(20) { createAccount() } + account)

        assertEquals(account.username, repo.getAccountByUserId(id).getOrThrow()?.username)
        assertEquals(account.userId, repo.getAccountByUsername(name).getOrThrow()?.userId)
        assertEquals(id, repo.getUserIdByUsername(name).getOrThrow())
        assertEquals(Credentials(id, account.hashedPassword), repo.getCredentials(name).getOrThrow())

        val newId = "id321"

        repo.updateUserAccount(id, account.copy(userId = newId))
        val a = repo.getAccountByUsername(name).getOrThrow()
        assertEquals(newId, a?.userId)

        repo.updateLastActivity(newId, 1000)
        assertEquals(1000, repo.getAccountByUsername(name).getOrThrow()?.lastActiveAt)

        assertTrue(repo.usernameExists(name).getOrThrow())
        assertTrue(repo.emailExists(email).getOrThrow())
    }
}
