package project.domain.cafe.topic

import encore.datastore.DocumentNotFoundException
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.utils.types.Outcome
import encore.utils.types.Report
import encore.utils.types.toOutcome
import encore.utils.types.toReport

/**
 * Server subunits that handles [Topic] concerns from [TopicRepository].
 * This subunit focuses on abstracting low-level API of `TopicRepository`.
 *
 * @property topicRepository [TopicRepository] implementation.
 */
class TopicSubunit(private val topicRepository: TopicRepository) : Subunit<ServerScope> {
    /**
     * Returns an [Outcome] containing the requested topic.
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the topic.
     */
    suspend fun getTopic(topicId: String): Outcome<Topic> {
        return topicRepository.getTopic(topicId)
            .onFailure {
                Fancam.error(it, "topic") {
                    "getTopic query failed for topicId=$topicId"
                }
            }
            .toOutcome { topic ->
                return if (topic == null) {
                    Outcome.Fail
                } else {
                    Outcome.Ok(topic)
                }
            }
    }

    /**
     * Returns an [Outcome] containing list of topics.
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the topics.
     */
    suspend fun getTopics(): Outcome<List<Topic>> {
        return topicRepository.getTopics()
            .onFailure { Fancam.error(it, "topic") { "getTopics query failed" } }
            .toOutcome { topics -> return Outcome.Ok(topics) }
    }

    /**
     * Add the [topic].
     * @return [Report] type denoting success or failure.
     */
    suspend fun addTopic(topic: Topic): Report {
        return topicRepository.addTopic(topic)
            .onFailure {
                Fancam.error(it, "topic") {
                    "addTopic failed for topic=$topic"
                }
            }
            .toReport()
    }

    /**
     * Delete the topic identified by [topicId].
     * @return [Outcome] type with [TopicDeletionOutcome].
     */
    suspend fun deleteTopic(topicId: String): Outcome<TopicDeletionOutcome> {
        val result = topicRepository.deleteTopic(topicId)
        return result
            .onFailure {
                Fancam.error(it, "topic") {
                    "deleteTopic failed for topicId=$topicId"
                }

                return when (it) {
                    is DocumentNotFoundException -> Outcome.Ok(TopicDeletionOutcome.TopicNotFound)
                    else -> Outcome.Fail
                }
            }
            .toOutcome { TopicDeletionOutcome.Success }
    }

    /**
     * Delete every topics in the database.
     * @return [Report] type denoting success or failure.
     */
    suspend fun deleteAllTopics(): Report {
        return topicRepository.deleteAllTopics()
            .onFailure {
                Fancam.error(it, "topic") {
                    "deleteAllTopics failed"
                }
            }
            .toReport()
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> {
        return runCatching { }
    }

    override suspend fun disband(scope: ServerScope): Result<Unit> {
        return runCatching { }
    }

    companion object {
        /**
         * Creates a test instance of [TopicSubunit].
         *
         * @param topicRepository use [InMemoryTopicRepository] when not under test.
         */
        fun createForTest(
            topicRepository: TopicRepository = InMemoryTopicRepository()
        ): TopicSubunit {
            return TopicSubunit(topicRepository)
        }
    }
}

/**
 * Represent outcome for topic deletion.
 * - [Success]
 * - [TopicNotFound]
 */
enum class TopicDeletionOutcome {
    /**
     * Topic deleted successfully.
     */
    Success,

    /**
     * Failed to delete topic because it wasn't found.
     */
    TopicNotFound
}
