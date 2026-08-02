package project.domain.cafe.topic.reply

import encore.datastore.DocumentNotFoundException
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.utils.types.Outcome
import encore.utils.types.Report
import encore.utils.types.toOutcome
import encore.utils.types.toReport

/**
 * Server subunits that handles [Reply] concerns from [ReplyRepository].
 */
class ReplySubunit(private val replyRepository: ReplyRepository) : Subunit<ServerScope> {
    /**
     * Returns an [Outcome] containing the requested reply.
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the reply, or null if it's not found.
     */
    suspend fun getReply(replyId: String): Outcome<Reply?> {
        return replyRepository.getReply(replyId)
            .onFailure {
                if (it !is DocumentNotFoundException) {
                    Fancam.error(it, "reply") {
                        "getReply query failed for replyId=$replyId"
                    }
                } else {
                    Fancam.error(it, "reply") {
                        "getReply reply not found for replyId=$replyId"
                    }
                }
            }
            .toOutcome { reply -> return Outcome.Ok(reply) }
    }

    /**
     * Returns an [Outcome] containing all replies of [topicId].
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the replies, or empty.
     */
    suspend fun getRepliesUnder(topicId: String): Outcome<List<Reply>> {
        return replyRepository.getRepliesUnder(topicId)
            .onFailure {
                Fancam.error(it, "reply") {
                    "getRepliesUnder query failed for topicId=$topicId"
                }
            }
            .toOutcome { replies -> return Outcome.Ok(replies) }
    }

    /**
     * Add the [reply].
     * @return [Report] type denoting success or failure.
     */
    suspend fun addReply(reply: Reply): Report {
        return replyRepository.addReply(reply)
            .onFailure {
                Fancam.error(it, "reply") {
                    "addReply failed for reply=$reply"
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
         * Creates a test instance of [ReplySubunit].
         * @param replyRepository use [InMemoryReplyRepository] when not under test.
         */
        fun createForTest(
            replyRepository: ReplyRepository = InMemoryReplyRepository()
        ): ReplySubunit {
            return ReplySubunit(replyRepository)
        }
    }
}
