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

    /**
     * Returns an [Outcome] containing the comment identified by [commentId]
     * which exists under the reply identified by [replyId].
     *
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the comment, or null if it's not found.
     */
    suspend fun getCommentById(replyId: String, commentId: String): Outcome<Comment?> {
        return replyRepository.getComments(replyId, 20)
            .onFailure {
                if (it !is DocumentNotFoundException) {
                    Fancam.error(it, "reply") {
                        "getCommentById query failed for replyId=$replyId"
                    }
                } else {
                    Fancam.warn("reply") {
                        "getCommentById reply not found for replyId=$replyId"
                    }
                }
            }
            .toOutcome { comments -> comments.find { it.commentId == commentId } }
    }

    /**
     * Returns an [Outcome] containing the comments under [replyId] limited by 20.
     * The limit is capped at 20, which is also the maximum number of comments
     * of a reply.
     *
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the replies, or empty.
     */
    suspend fun getCommentsUnder(replyId: String, limit: Int): Outcome<List<Comment>> {
        return replyRepository.getComments(replyId, minOf(20, limit))
            .onFailure {
                Fancam.error(it, "reply") {
                    "getCommentsUnder query failed for replyId=$replyId"
                }
            }
            .toOutcome { it }
    }

    /**
     * Add the [comment] to the reply identified by [replyId].
     * @return [Report] type denoting success or failure.
     */
    suspend fun addComment(replyId: String, comment: Comment): Report {
        return replyRepository.addComment(replyId, comment)
            .onFailure {
                if (it !is DocumentNotFoundException) {
                    Fancam.error(it, "reply") {
                        "addComment query failed for replyId=$replyId with comment=$comment"
                    }
                } else {
                    Fancam.warn("reply") {
                        "addComment reply not found for replyId=$replyId"
                    }
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
