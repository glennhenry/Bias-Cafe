package projectTest

import TestMongoCollectionName
import initMongo
import io.ktor.util.date.*
import kotlinx.coroutines.test.runTest
import project.domain.cafe.topic.MongoTopicRepository
import project.domain.cafe.topic.Topic
import testUtils.randomString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test operations of [MongoTopicRepository].
 */
class MongoTopicRepositoryTest {
    @Test
    fun `test all`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<Topic>(TestMongoCollectionName.topic)
        collection.drop()
        mongoDb.createCollection(TestMongoCollectionName.topic)

        val repo = MongoTopicRepository(collection)

        // setup
        val targetTopic = Topic("topicId123", "sectionId123", "title123", "author123", "content123", 0)
        collection.insertMany(createTopic(20) + targetTopic)

        // tests
        // 1. getTopic
        assertNotNull(repo.getTopic("topicId123").getOrNull())

        // 2. getTopics
        assertNotNull(
            repo.getTopics().getOrThrow().find { it.topicId == targetTopic.topicId }
        )

        // 3. getTopicsOfSection
        assertNotNull(
            repo.getTopicsOfSection("sectionId123").getOrThrow().find { it.sectionId == targetTopic.sectionId }
        )

        // 4. getTopicsCountForEachSection
        assertEquals(1, repo.getTopicsCountForEachSection().getOrThrow()["sectionId123"])

        // 5. addTopic
        assertTrue(
            repo.addTopic(
                Topic(
                    "topicId456",
                    "sectionId456",
                    "title456",
                    "author456",
                    "content456",
                    0
                )
            ).isSuccess
        )
        assertNotNull(repo.getTopic("topicId456"))

        // 6. deleteTopic
        assertTrue(repo.deleteTopic("topicId456").isSuccess)
        assertTrue(repo.getTopic("topicId456").isFailure)
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
