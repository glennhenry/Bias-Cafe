package projectTest

import TestMongoCollectionName
import encore.datastore.collection.Topic
import initMongo
import kotlinx.coroutines.test.runTest
import project.domain.cafe.topic.MongoTopicRepository
import testUtils.randomString
import kotlin.test.Test
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
        val targetTopic = Topic("topicId123", "content123")
        collection.insertMany(createTopic(20) + targetTopic)

        // tests
        // 1. getTopic
        assertNotNull(repo.getTopic("topicId123").getOrNull())

        // 2. getTopics
        assertNotNull(
            repo.getTopics().getOrThrow().find { it.topicId == targetTopic.topicId }
        )

        // 3. addTopic
        assertTrue(repo.addTopic(Topic("topicId456", "content456")).isSuccess)
        assertNotNull(repo.getTopic("topicId456"))

        // 4. deleteTopic
        assertTrue(repo.deleteTopic("topicId456").isSuccess)
        assertTrue(repo.getTopic("topicId456").isFailure)
    }

    private fun createTopic(amount: Int): List<Topic> {
        return List(amount) {
            Topic(randstr(), randstr())
        }
    }

    private val charpool = ('a'..'z').toList()
    private fun randstr(): String {
        return randomString(8, charpool)
    }
}
