package encoreTest.account

import TestMongoCollectionName
import encore.account.MongoAccountRepository
import encore.account.model.Credentials
import encore.account.model.UserMetadata
import encore.datastore.collection.Profile
import encore.datastore.collection.UserAccount
import encore.utils.identifier.Ids
import encore.utils.hash
import initMongo
import kotlinx.coroutines.test.runTest
import testUtils.*
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
        val collection = mongoDb.getCollection<UserAccount>(TestMongoCollectionName.userAccount)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.userAccount)

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
            metadata = UserMetadata(),
        )

        collection.insertMany(List(20) { account() } + account)

        assertEquals(account.userId, repo.getAccountByUsername(name).getOrThrow().userId)
        assertEquals(id, repo.getUserIdByUsername(name).getOrThrow())
        assertEquals(Credentials(id, account.hashedPassword), repo.getCredentials(name).getOrThrow())

        val newId = "id321"

        repo.updateUserAccount(id, account.copy(userId = newId))
        val a = repo.getAccountByUsername(name).getOrThrow()
        assertEquals(newId, a.userId)

        repo.updateLastActivity(newId, 1000)
        assertEquals(1000, repo.getAccountByUsername(name).getOrThrow().lastActiveAt)

        assertTrue(repo.usernameExists(name).getOrThrow())
        assertTrue(repo.emailExists(email).getOrThrow())
    }

    private fun account(): UserAccount {
        return createAccount(Ids.uuid(), randstr(), randstr())
    }

    private val charpool = ('a'..'z').toList()
    private fun randstr(): String {
        return randomString(8, charpool)
    }
}
