package project.domain.cafe.topic

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.DocumentNotFoundException
import encore.datastore.collection.Topic
import encore.datastore.runMongoCatching
import encore.fancam.Fancam
import encore.venue.Venue
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

/** `topicId`*/
val FieldTopicId = Topic::topicId.name

/** `postedAt` */
val FieldPostedAt = Topic::postedAt.name

class MongoTopicRepository(private val topicCollection: MongoCollection<Topic>) : TopicRepository {
    override suspend fun awaitInit() {
        if (Venue.custom.deletePostsEveryRestart) {
            topicCollection.drop()
            Fancam.info("mongotopic") { "Topic collection reseted" }
        }

        if (topicCollection.estimatedDocumentCount() < 10 && Venue.custom.prepareDummyPosts) {
            if (topicCollection.insertMany(TopicFactory.dummyTopics(20)).wasAcknowledged()) {
                Fancam.info("mongotopic") { "Inserted 20 dummy posts successfully" }
            }
        }
    }

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
            val result = topicCollection.deleteOne(Filters.eq(FieldTopicId, topicId))
            if (!result.wasAcknowledged()) {
                throw IllegalStateException("Topic deletion not acknowledged")
            }

            if (result.deletedCount <= 0) {
                throw DocumentNotFoundException("Topic $topicId is not found")
            }
        }
    }

    override suspend fun deleteAllTopics(): Result<Unit> {
        return runMongoCatching { topicCollection.drop() }
    }
}
