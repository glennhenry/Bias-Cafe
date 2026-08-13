package portal.domain.dummy

import encore.time.TimeCenter
import encore.utils.identifier.Ids
import portal.domain.Members
import portal.domain.cafe.topic.reply.Comment
import portal.domain.cafe.topic.reply.Reply
import kotlin.random.Random

/**
 * Utilities to create dummy replies and comments for topic.
 */
object ReplyFactory {
    /**
     * @param topicId The `topicId` to which this reply belongs to.
     * @param topicPostDate The time of when the topic was posted.
     * @param possibleReplyAuthors Picks a random author that can own this reply.
     * @param possibleAmountofComments A range of how many comment for this reply.
     * @param possibleCommentAuthors Picks a random author that can own one of the comment.
     */
    fun reply(
        topicId: String, topicPostDate: Long, possibleReplyAuthors: List<String>,
        possibleAmountofComments: IntRange, possibleCommentAuthors: List<String>,
    ): Reply {
        val replyPostDate = postedDate(topicPostDate)
        return Reply(
            replyId = Ids.uuid(),
            topicId = topicId,
            authorId = possibleReplyAuthors.random(),
            content = content(),
            postedDate = replyPostDate,
            comments = comments(possibleCommentAuthors, replyPostDate, possibleAmountofComments)
        )
    }

    private fun comments(
        authors: List<String>,
        replyPostDate: Long,
        possibleAmountofComments: IntRange
    ): List<Comment> {
        if (Random.nextBoolean()) {
            val amount = (maxOf(possibleAmountofComments.first, 0)..possibleAmountofComments.last)
                .random()
            return List(amount) {
                comment(authorId = authors.random(), replyPostDate)
            }.sortedBy { it.postedDate }
        } else {
            return emptyList()
        }
    }

    private fun comment(authorId: String, replyPostDate: Long): Comment {
        return Comment(
            commentId = Ids.uuid(),
            authorId = authorId,
            content = commentContent(),
            postedDate = postedDate(replyPostDate)
        )
    }

    private fun content(member: String = Members.all.random()): String {
        return "I ${Words.verb()} $member so much."
    }

    private val commentContents = listOf(
        "I agree.", "Yes she is.", "I think the same", "You are right", "Me too.",
        "I like her", "I love her", "Of course she is awesome", "😀😀", "Okay."
    )

    private fun commentContent(): String {
        return commentContents.random()
    }

    /**
     * Get a random `postedDate` from now up to the [limit].
     */
    private fun postedDate(limit: Long): Long {
        val range = limit.rangeTo(TimeCenter.now())
        return range.random()
    }
}
