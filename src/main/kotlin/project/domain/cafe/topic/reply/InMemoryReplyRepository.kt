package project.domain.cafe.topic.reply

/**
 * In-memory implementation for [ReplyRepository].
 */
class InMemoryReplyRepository(
    private val replies: MutableList<Reply> = mutableListOf()
) : ReplyRepository {
    override suspend fun awaitInit() = Unit

    override suspend fun getReply(replyId: String): Result<Reply?> {
        return Result.success(replies.find { it.replyId == replyId })
    }

    override suspend fun getRepliesUnder(topicId: String): Result<List<Reply>> {
        return Result.success(replies.filter { it.topicId == topicId })
    }

    override suspend fun getReplyCount(topicId: String): Result<Int?> {
        return Result.success(replies.count { it.topicId == topicId })
    }

    override suspend fun getReplyCounts(topicIds: List<String>): Result<Map<String, Int?>> {
        return Result.success(replies.groupingBy { it.topicId }.eachCount())
    }

    override suspend fun addReply(reply: Reply): Result<Unit> {
        replies.add(reply)
        return Result.success(Unit)
    }

    override suspend fun getComments(
        replyId: String,
        limit: Int
    ): Result<List<Comment>> {
        return Result.success(
            replies
                .find { it.replyId == replyId }
                ?.comments
                ?.take(limit)
                .orEmpty()
        )
    }

    override suspend fun addComment(
        replyId: String,
        comment: Comment
    ): Result<Unit> {
        val reply = replies.find { it.replyId == replyId }
            ?: return Result.failure(Exception("replyId=$replyId not found."))
        replies.removeIf { it.replyId == replyId }
        replies.add(reply.copy(comments = reply.comments + comment))
        return Result.success(Unit)
    }
}
