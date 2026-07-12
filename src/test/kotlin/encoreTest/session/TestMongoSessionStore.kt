package encoreTest.session

import TestMongoCollectionName
import project.domain.session.MongoSessionStore
import project.domain.session.SessionStoreModel
import encore.utils.identifier.Ids
import initMongo
import io.ktor.util.date.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class TestMongoSessionStore {
    @Test
    fun `test all`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<SessionStoreModel>(TestMongoCollectionName.websiteSession)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.websiteSession)

        val store = MongoSessionStore(collection)

        val dummy = List(20) {
            SessionStoreModel(Ids.uuid(), getTimeMillis() + 100.seconds.inWholeMilliseconds)
        }

        collection.insertMany(dummy)
        collection.insertOne(SessionStoreModel("abcdefgh", 1))

        // 1. load
        val loadResult = store.load().getOrThrow()
        assertEquals(21, loadResult.size)
        assertNotNull(loadResult.find { it.token == "abcdefgh" && it.expiresAt == 1L })

        // 2. put
        val putResult = store.put("xyz123", 2)
        assertTrue(putResult.isSuccess)

        // 3. update
        val updateResult = store.update("xyz123", 1000)
        assertTrue(updateResult.isSuccess)
        assertNotNull(
            store.load().getOrThrow().find { it.token == "xyz123" && it.expiresAt == 1000L }
        )

        // 4. delete
        val deleteResult = store.delete("xyz123")
        assertTrue(deleteResult.isSuccess)

        // 5. batchDeleteExpiredSessions
        val result = store.batchDeleteExpiredSessions(getTimeMillis())
        assertTrue(result.isSuccess)
        val loadResult2 = store.load().getOrThrow()
        assertEquals(20, loadResult2.size)
        assertNull(loadResult2.find { it.token == "abcdefgh" })
    }
}
