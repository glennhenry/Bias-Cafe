package projectTest

import TestMongoCollectionName
import encore.datastore.collection.Topic
import initMongo
import kotlinx.coroutines.test.runTest
import project.domain.cafe.MongoTopicRepository
import testUtils.randomString
import kotlin.test.Test
import kotlin.test.assertNotNull

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
        assertNotNull(
            repo.getTopics().getOrThrow().find { it.topicId == targetTopic.topicId }
        )
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
