package portal.domain.cafe.view.model

import portal.domain.cafe.topic.Topic
import portal.domain.cafe.reply.Reply
import portal.domain.cafe.reply.Comment
import portal.routes.common.AccountData

/**
 * Model to display a topic post in the cafe. This uses the `topicview.html`.
 *
 * @property account Optional [AccountData] of the user.
 * @property sectionName The name of section to which this topic belongs to. Uses [SectionItem.name].
 * @property topicId The unique identifier of the topic. Uses [Topic.topicId]
 * @property topic The detailed data of the topic.
 * @property replies List of replies within this topic.
 */
data class TopicViewModel(
    val account: AccountData?,
    val sectionName: String,
    val topicId: String,
    val topic: TopicViewData,
    val replies: List<ReplyData>
)

/**
 * Data of topic.
 *
 * @property title The title of the topic. Uses [Topic.title].
 * @property authorDisplayName The author's display name that posted this topic.
 * @property authorAvatarUrl The author's avatar url that posted this topic.
 * @property postedDate The date of when this topic was posted. Uses [Topic.postedDate].
 * @property content The content of the topic. Uses [Topic.content].
 */
data class TopicViewData(
    val title: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String,
    val postedDate: Long,
    val content: String
)

/**
 * Data of reply.
 *
 * @property replyId The unique identifier of the reply. Uses [Reply.replyId].
 * @property authorDisplayName The author's display name that posted this reply.
 * @property authorAvatarUrl The author's avatar url that posted this reply.
 * @property postedDate The date of when this reply was posted. Uses [Reply.postedDate].
 * @property content The content of the reply. Uses [Reply.content].
 * @property comments The comments of this reply.
 */
data class ReplyData(
    val replyId: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String,
    val postedDate: Long,
    val content: String,
    val comments: List<CommentData>
)

/**
 * Data of comment.
 *
 * @property commentId The unique identifier of the comment. Uses [Comment.commentId].
 * @property authorDisplayName The author's display name that posted this comment.
 * @property authorAvatarUrl The author's avatar url that posted this comment.
 * @property postedDate The date of when this comment was posted. Uses [Comment.postedDate].
 * @property content The content of the comment. Uses [Comment.content].
 */
data class CommentData(
    val commentId: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String,
    val postedDate: Long,
    val content: String
)
