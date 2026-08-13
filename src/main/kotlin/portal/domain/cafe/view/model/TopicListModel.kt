package portal.domain.cafe.view.model

import portal.domain.cafe.topic.Topic
import portal.routes.common.AccountData

/**
 * Model to display the list of topics within a section in the cafe.
 * This uses the `topiclist.html`; URLs examples: `/cafe/kep1er`, `/cafe/kpop`.
 *
 * @property account Optional [AccountData] of the user.
 * @property sectionId The unique identifier of the section. Uses [SectionItem.id].
 * @property topics The list of topics that this section contains.
 */
data class TopicListModel(
    val account: AccountData?,
    val sectionId: String,
    val topics: List<TopicListItemData> = emptyList()
)

/**
 * A single topic item within the list of topics in a section.
 *
 * @property topicId Unique identifier of the topic. Uses [Topic.topicId].
 * @property link The link that direct to the topic's page.
 * @property title Title of the topic. Uses [Topic.title].
 * @property authorName The name of author that posted the topic.
 * @property replyCount The amount of reply posted within this topic.
 * @property postedDate The date of when this topic was posted. Uses [Topic.postedDate].
 */
data class TopicListItemData(
    val topicId: String,
    val link: String,
    val title: String,
    val authorName: String,
    val replyCount: Int,
    val postedDate: Long
)
