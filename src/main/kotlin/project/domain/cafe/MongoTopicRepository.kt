package project.domain.cafe

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.collection.Topic
import encore.datastore.runMongoCatching
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

/** `topicId`*/
val FieldTopicId = Topic::topicId.name
/** `postedAt` */
val FieldPostedAt = Topic::postedAt.name

class MongoTopicRepository(private val topicCollection: MongoCollection<Topic>) : TopicRepository {
    override suspend fun getTopic(topicId: String): Result<Topic?> {
        return runMongoCatching {
            topicCollection
                .find(Filters.eq(FieldTopicId, topicId))
                .firstOrNull()
        }
    }

    override suspend fun getTopics(): Result<List<Topic>> {
        return runMongoCatching {
            topicCollection
                .find()
                .sort(Sorts.descending(FieldPostedAt))
                .toList()
        }
    }

    override suspend fun addTopic(topic: Topic): Result<Unit> {
        return runMongoCatching {
            if (!topicCollection.insertOne(topic).wasAcknowledged()) {
                throw IllegalStateException("Topic insertion not acknowledged")
            }
        }
    }

    override suspend fun deleteTopic(topicId: String): Result<Unit> {
        return runMongoCatching {
            if (!topicCollection.deleteOne(Filters.eq(FieldTopicId, topicId)).wasAcknowledged()) {
                throw IllegalStateException("Topic deletion not acknowledged")
            }
        }
    }

    override suspend fun deleteAllTopics(): Result<Unit> {
        return runMongoCatching { topicCollection.drop() }
    }
}
