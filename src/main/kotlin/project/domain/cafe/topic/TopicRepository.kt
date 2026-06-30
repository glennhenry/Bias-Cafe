package project.domain.cafe.topic

import encore.datastore.collection.Topic

/**
 * Repository for [Topic] collection.
 *
 * Implementation abstract access to the topics underlying store.
 */
interface TopicRepository {
    /**
     * Ensures the repository is fully initialized.
     *
     * Use this for repository initialization that may utilize suspendable code.
     */
    suspend fun awaitInit()

    /**
     * Get the topic identified by [topicId].
     *
     * Returns:
     * - [Result.success] with the topic.
     * - [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getTopic(topicId: String): Result<Topic?>

    /**
     * Get every available topics.
     *
     * Returns:
     * - [Result.success] with the list of topic.
     * - [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getTopics(): Result<List<Topic>>

    /**
     * Add the [topic].
     *
     * Returns:
     * - [Result.success] if the operation succeeded.
     * - [Result.failure] if an error occurs during the operation.
     */
    suspend fun addTopic(topic: Topic): Result<Unit>

    /**
     * Delete the topic identified by [topicId].
     *
     * Returns:
     * - [Result.success] if the operation succeeded.
     * - [Result.failure] if an error occurs during the operation.
     */
    suspend fun deleteTopic(topicId: String): Result<Unit>

    /**
     * Delete all topics.
     *
     * Returns:
     * - [Result.success] if the operation succeeded.
     * - [Result.failure] if an error occurs during the operation.
     */
    suspend fun deleteAllTopics(): Result<Unit>
}
