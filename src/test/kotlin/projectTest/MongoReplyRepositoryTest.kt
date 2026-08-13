package projectTest

import TestCollections
import initMongo
import io.ktor.util.date.*
import kotlinx.coroutines.test.runTest
import portal.domain.cafe.topic.reply.Comment
import portal.domain.cafe.topic.reply.MongoReplyRepository
import portal.domain.cafe.topic.reply.Reply
import testUtils.randomString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        val targetReply = Reply(
            replyId = id,
            topicId = "fixedTopicId",
            authorId = "author123",
            content = "content123",
            postedDate = 0,
            comments = listOf(
                Comment("comment1", "author1", "hello world 1", 0),
                Comment("comment2", "author2", "hello world 2", 0),
                Comment("comment3", "author3", "hello world 3", 0),
                Comment("comment4", "author4", "hello world 4", 0),
                Comment("comment5", "author5", "hello world 5", 0),
                Comment("comment6", "author6", "hello world 6", 0),
            )
        )
        collection.insertMany(createReply(10, "yesyes") + createReply(10, "fixedTopicId") + targetReply)

        // tests
        // 1. getReply
        assertNotNull(repo.getReply(id).getOrNull())

        // 2. getRepliesUnder
        assertEquals(11, repo.getRepliesUnder("fixedTopicId").getOrNull()?.size)

        // 3. getReplyCount
        assertEquals(11, repo.getReplyCount(topicId = "fixedTopicId").getOrThrow())

        // 4. getReplyCounts
        assertTrue {
            val x = repo.getReplyCounts(listOf("fixedTopicId", "yesyes")).getOrThrow()
            x["fixedTopicId"]!! == 11 && x["yesyes"]!! == 10
        }

        // 5. addReply
        assertNotNull(repo.addReply(Reply("asdf", "asdf", "asdf", "asdf", 0, emptyList())).getOrNull())
        assertNotNull(repo.getReply("asdf").getOrNull())

        // 6. getComments
        assertTrue {
            val x = repo.getComments(id, 3).getOrThrow()
            x.find { it.commentId == "comment1" } != null &&
                    x.find { it.commentId == "comment2" } != null &&
                    x.find { it.commentId == "comment3" } != null
        }

        // 5. addComment
        repo.addComment(id, comment = Comment("comment7", "author7", "hello world 7", 0)).getOrThrow()
        assertNotNull(repo.getComments(id, 7).getOrThrow().find { it.commentId == "comment7" })
    }

    private fun createReply(amount: Int, topicId: String = randstr()): List<Reply> {
        return List(amount) {
            Reply(randstr(), topicId, randstr(), randstr(), getTimeMillis(), emptyList())
        }
    }

    private val charpool = ('a'..'z').toList()
    private fun randstr(): String {
        return randomString(8, charpool)
    }
}
