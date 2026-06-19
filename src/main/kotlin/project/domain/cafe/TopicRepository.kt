package project.domain.cafe

import encore.datastore.collection.Topic

/**
 * Repository for [Topic] collection.
 *
 * Implementation abstract access to the topics underlying store.
 */
interface TopicRepository {
    /**
     * Get every available topics.
     *
     * Returns:
     * - [Result.success] with the list of topic.
     * - [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getTopics(): Result<List<Topic>>
}
