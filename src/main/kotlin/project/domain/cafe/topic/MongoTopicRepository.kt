package project.domain.cafe.topic

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.runMongoCatching
import encore.datastore.throwIfNothingDeleted
import encore.utils.support.asUnit
import kotlinx.coroutines.flow.associate
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId

/** `topicId`*/
val FieldTopicId = Topic::topicId.name

/** `postedDate` */
val FieldPostedDate = Topic::postedDate.name

/** `sectionId` */
val FieldSectionId = Topic::sectionId.name

class MongoTopicRepository(private val topicCollection: MongoCollection<Topic>) : TopicRepository {
    override suspend fun awaitInit() {
        topicCollection.createIndex(Indexes.text())
    }

    override suspend fun getTopic(topicId: String): Result<Topic?> {
        return runMongoCatching {
            topicCollection
                .find(Filters.eq(FieldTopicId, topicId))
                .firstOrNull()
        }
    }

    override suspend fun getTopicByShortId(shortTopicId: String): Result<Topic?> {
        return runMongoCatching {
            topicCollection
                .find(Filters.regex(FieldTopicId, "^$shortTopicId"))
                .firstOrNull()
        }
    }

    override suspend fun getFullTopicId(shortTopicId: String): Result<String?> {
        return runMongoCatching {
            topicCollection
                .withDocumentClass<QueryTopicId>()
                .find(Filters.regex(FieldTopicId, "^$shortTopicId"))
                .projection(
                    Projections.fields(
                        Projections.include(FieldTopicId),
                        Projections.excludeId()
                    )
                )
                .firstOrNull()
                ?.topicId
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
            topicCollection.insertOne(topic).asUnit()
        }
    }

    override suspend fun deleteTopic(topicId: String): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldTopicId, topicId)
            topicCollection.deleteOne(filter)
                .throwIfNothingDeleted("deleteTopic", { filter })
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

data class QueryTopicId(
    @field:BsonId val id: String? = null,
    val topicId: String
)
