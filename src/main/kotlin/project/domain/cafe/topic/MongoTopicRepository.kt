package project.domain.cafe.topic

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.DocumentNotFoundException
import encore.datastore.ensureAck
import encore.datastore.runMongoCatching
import encore.fancam.Fancam
import encore.utils.support.asUnit
import encore.venue.Venue
import kotlinx.coroutines.flow.associate
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

/** `topicId`*/
val FieldTopicId = Topic::topicId.name

/** `postedDate` */
val FieldPostedDate = Topic::postedDate.name

/** `sectionId` */
val FieldSectionId = Topic::sectionId.name

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
                .sort(Sorts.descending(FieldPostedDate))
                .toList()
        }
    }

    override suspend fun getTopicsOfSection(sectionId: String): Result<List<Topic>> {
        return runMongoCatching {
            topicCollection
                .find(Filters.eq(FieldSectionId, sectionId))
                .sort(Sorts.descending(FieldPostedDate))
                .toList()
        }
    }

    override suspend fun getTopicsCountForEachSection(): Result<Map<String, Int>> {
        return runMongoCatching {
            topicCollection
                .withDocumentClass<SectionCount>()
                .aggregate(
                    listOf(
                        Aggregates.group("$$FieldSectionId", Accumulators.sum("count", 1)),
                        Aggregates.project(
                            Projections.fields(
                                Projections.computed("sectionId", $$"$_id"),
                                Projections.include("count")
                            )
                        )
                    )
                ).associate { it.sectionId to it.count }
        }
    }

    override suspend fun addTopic(topic: Topic): Result<Unit> {
        return runMongoCatching {
            ensureAck(topicCollection.insertOne(topic))
                .asUnit()
        }
    }

    override suspend fun deleteTopic(topicId: String): Result<Unit> {
        return runMongoCatching {
            if (ensureAck(topicCollection.deleteOne(Filters.eq(FieldTopicId, topicId))).deletedCount <= 0) {
                error("Topic $topicId wasn't deleted (deletedCount <= 0)")
            }
        }
    }

    override suspend fun deleteAllTopics(): Result<Unit> {
        return runMongoCatching { topicCollection.drop() }
    }
}

data class SectionCount(
    val sectionId: String,
    val count: Int
)
