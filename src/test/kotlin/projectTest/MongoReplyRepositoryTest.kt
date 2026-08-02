package projectTest

import TestCollections
import initMongo
import io.ktor.util.date.*
import kotlinx.coroutines.test.runTest
import project.domain.cafe.topic.reply.MongoReplyRepository
import project.domain.cafe.topic.reply.Reply
import testUtils.randomString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Test operations of [MongoReplyRepository].
 */
class MongoReplyRepositoryTest {
    @Test
    fun `test all`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<Reply>(TestCollections.reply)
        collection.drop()
        mongoDb.createCollection(TestCollections.reply)

        val repo = MongoReplyRepository(collection)

        // setup
        val id = "5ab0980c-e2cb-990a-427a-5ad9b0311b7f"
        val targetReply = Reply(id, "fixedTopicId", null, "author123", "content123", 0)
        collection.insertMany(createReply(10) + createReply(10, "fixedTopicId") + targetReply)

        // tests
        // 1. getReply
        assertNotNull(repo.getReply(id).getOrNull())

        // 2. getRepliesUnder
        assertEquals(11, repo.getRepliesUnder("fixedTopicId").getOrNull()?.size)

        // 3. addReply
        assertNotNull(repo.addReply(Reply("asdf", "asdf", "asdf", "asdf", "asdf", 0)).getOrNull())
        assertNotNull(repo.getReply("asdf").getOrNull())
    }

    private fun createReply(amount: Int, topicId: String = randstr()): List<Reply> {
        return List(amount) {
            Reply(randstr(), topicId, randstr(), randstr(), randstr(), getTimeMillis())
        }
    }

    private val charpool = ('a'..'z').toList()
    private fun randstr(): String {
        return randomString(8, charpool)
    }
}
