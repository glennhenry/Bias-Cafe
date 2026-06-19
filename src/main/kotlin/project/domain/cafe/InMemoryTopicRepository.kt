package project.domain.cafe

import encore.datastore.collection.Topic

/**
 * In-memory implementation for [TopicRepository].
 */
class InMemoryTopicRepository(
    private val topics: MutableList<Topic> = mutableListOf()
) : TopicRepository {
    override suspend fun getTopics(): Result<List<Topic>> {
        return Result.success(topics)
    }
}
