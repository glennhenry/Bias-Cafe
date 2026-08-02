package projectTest

import TestCollections
import encore.utils.identifier.shortUuid
import initMongo
import io.ktor.util.date.*
import kotlinx.coroutines.test.runTest
import project.domain.cafe.topic.MongoTopicRepository
import project.domain.cafe.topic.Topic
import testUtils.randomString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Test operations of [MongoTopicRepository].
 */
class MongoTopicRepositoryTest {
    @Test
    fun `test all`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<Topic>(TestCollections.topic)
        collection.drop()
        mongoDb.createCollection(TestCollections.topic)

        val repo = MongoTopicRepository(collection)

        // setup
        val id = "5ab0980c-e2cb-990a-427a-5ad9b0311b7f"
        val targetTopic = Topic(id, "sectionId123", "title123", "author123", "content123", 0)
        collection.insertMany(createTopic(20) + targetTopic)

        // tests
        // 1. getTopic
        assertNotNull(repo.getTopic(id).getOrNull())

        // 2. getTopicByShortId
        assertNotNull(repo.getTopicByShortId(id.shortUuid()).getOrNull())

        // 3. getTopicByShortId
        assertEquals(id, repo.getFullTopicId(id.shortUuid()).getOrNull())

        // 4. getTopics
        assertNotNull(
            repo.getTopics().getOrThrow().find { it.topicId == targetTopic.topicId }
        )

        // 5. getTopicsOfSection
        assertNotNull(
            repo.getTopicsOfSection("sectionId123").getOrThrow().find { it.sectionId == targetTopic.sectionId }
        )

        // 6. getTopicsCountForEachSection
        assertEquals(1, repo.getTopicsCountForEachSection().getOrThrow()["sectionId123"])

        // 7. addTopic
        val t = Topic("asdf", "asdf", "asdf", "asdf", "asdf", 0)
        assertNotNull(repo.addTopic(t).getOrNull())
        assertNotNull(repo.getTopic("asdf").getOrNull())

        // 8. deleteTopic
        assertNotNull(repo.deleteTopic("asdf").getOrNull())
        assertNull(repo.getTopic("asdf").getOrNull())
    }

    private fun createTopic(amount: Int): List<Topic> {
        return List(amount) {
            Topic(randstr(), randstr(), randstr(), randstr(), randstr(), getTimeMillis())
        }
    }

    private val charpool = ('a'..'z').toList()
    private fun randstr(): String {
        return randomString(8, charpool)
    }
}
