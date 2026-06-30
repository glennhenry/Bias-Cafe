package project.domain.cafe.topic

import encore.datastore.collection.Topic

/**
 * In-memory implementation for [TopicRepository].
 */
class InMemoryTopicRepository(
    private val topics: MutableList<Topic> = mutableListOf()
) : TopicRepository {
    override suspend fun awaitInit() = Unit

    override suspend fun getTopic(topicId: String): Result<Topic?> {
        return Result.success(topics.find { it.topicId == topicId })
    }

    override suspend fun getTopics(): Result<List<Topic>> {
        return Result.success(topics)
    }

    override suspend fun addTopic(topic: Topic): Result<Unit> {
        topics.add(topic)
        return Result.success(Unit)
    }

    override suspend fun deleteTopic(topicId: String): Result<Unit> {
        topics.removeIf { it.topicId == topicId }
        return Result.success(Unit)
    }

    override suspend fun deleteAllTopics(): Result<Unit> {
        topics.clear()
        return Result.success(Unit)
    }
}
