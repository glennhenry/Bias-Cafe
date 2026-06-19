package project.domain.cafe

import encore.datastore.collection.Topic
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.utils.types.Outcome
import encore.utils.types.toOutcome

/**
 * Server subunits that handles [Topic] concerns from [TopicRepository].
 * This subunit focuses on abstracting low-level API of `TopicRepository`.
 *
 * @property topicRepository [TopicRepository] implementation.
 */
class TopicSubunit(private val topicRepository: TopicRepository) : Subunit<ServerScope> {
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
