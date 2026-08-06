package project.domain.cafe.topic.reply

/**
 * Repository for [Reply] collection.
 */
interface ReplyRepository {
    /**
     * Ensures the repository is fully initialized.
     *
     * Use this for repository initialization that may utilize suspendable code.
     */
    suspend fun awaitInit()

    /**
     * Get the reply identified by [replyId].
     *
     * Returns:
     * - [Result.success] with the reply.
     * - [Result.success] with `null` if not found.
     * - [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getReply(replyId: String): Result<Reply?>

    /**
     * Get all the replies under certain topic identified by its [topicId].
     *
     * Returns:
     * - [Result.success] with the replies or empty.
     * - [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getRepliesUnder(topicId: String): Result<List<Reply>>

    /**
     * Add the [reply].
     *
     * Returns:
     * - [Result.success] if the operation succeeded.
     * - [Result.failure] if an error occurs during the operation.
     */
    suspend fun addReply(reply: Reply): Result<Unit>

    /**
     * Get the comments under the reply identified by [replyId]
     * limited by [limit].
     *
     * Returns:
     * - [Result.success] with a list of comments, or empty.
     * - [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getComments(replyId: String, limit: Int): Result<List<Comment>>

    /**
     * Add the [comment] to the reply identified by [replyId].
     *
     * Returns:
     * - [Result.success] if the operation succeeded.
     * - [Result.failure] if an error occurs during the operation.
     */
    suspend fun addComment(replyId: String, comment: Comment): Result<Unit>
}
