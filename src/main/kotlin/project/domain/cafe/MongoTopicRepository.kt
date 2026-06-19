package project.domain.cafe

import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.collection.Topic
import encore.datastore.runMongoCatching
import kotlinx.coroutines.flow.toList

class MongoTopicRepository(private val topicCollection: MongoCollection<Topic>) : TopicRepository {
    override suspend fun getTopics(): Result<List<Topic>> {
        return runMongoCatching {
            topicCollection.find().toList()
        }
    }
}
