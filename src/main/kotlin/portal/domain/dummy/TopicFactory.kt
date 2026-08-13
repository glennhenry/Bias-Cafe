package portal.domain.dummy

import encore.time.TimeCenter
import encore.utils.identifier.Ids
import portal.domain.Members
import portal.domain.cafe.topic.Topic
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

/**
 * Utilities to create dummy topics.
 */
object TopicFactory {
    private val sections = listOf(
        "kep1er", "kpop",
        "yujin", "xiaoting", "mashiro",
        "chaehyun", "dayeon", "hikaru",
        "hiyyih", "youngeun", "yeseo",
        "media", "games"
    )

    fun topics(authorId: String, amount: Int): List<Topic> {
        return List(amount) {
            val member = Members.all.random()
            Topic(
                topicId = Ids.uuid(),
                sectionId = sections.random(),
                title = title(member),
                authorId = authorId,
                content = content(member),
                postedDate = postedDate()
            )
        }
    }

    private fun title(member: String = Members.all.random()): String {
        return "$member is ${Words.capitalAdjective()}"
    }

    private fun content(member: String = Members.all.random()): String {
        return "I ${Words.verb()} $member so much."
    }

    /**
     * Get a random `postedDate` from now up to 14 days ago.
     */
    private fun postedDate(): Long {
        val now = TimeCenter.now()
        val from = now.milliseconds - 14.days
        val range = from.inWholeMilliseconds.rangeTo(now)
        return range.random()
    }
}
