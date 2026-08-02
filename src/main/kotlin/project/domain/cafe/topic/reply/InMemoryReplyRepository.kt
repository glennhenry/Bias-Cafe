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

    override suspend fun addReply(reply: Reply): Result<Unit> {
        replies.add(reply)
        return Result.success(Unit)
    }
}
