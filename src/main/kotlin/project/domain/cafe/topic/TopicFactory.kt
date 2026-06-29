package project.domain.cafe.topic

import encore.datastore.collection.Topic
import encore.time.TimeCenter
import encore.utils.identifier.Ids
import project.Members

/**
 * Utilities to create dummy topics.
 */
object TopicFactory {
    fun dummyTopics(amount: Int): List<Topic> {
        return List(amount) {
            val member = Members.all.random()
            Topic(
                topicId = Ids.uuid(),
                title = dummyTitle(member),
                author = dummyAuthor(),
                content = dummyContent(member),
                postedAt = TimeCenter.now()
            )
        }
    }

    private val adjectives = listOf(
        "cute", "talented", "beautiful", "lovely", "best",
        "smart", "classy", "dazzling", "gorgeous", "cool",
        "pretty", "kind", "ambitious", "bright", "clever",
        "perfect", "elegant", "radiant", "graceful", "nice",
    )

    private val nouns = listOf(
        "orange", "tomato", "veggies", "handphone", "gadget",
        "sandwich", "tank", "burger", "chicken", "cow",
        "hotdog", "water", "lily", "glass", "coffee",
        "beef", "chili", "roof", "zombie", "car",
    )

    private val verbs = listOf(
        "love", "like", "bias", "admire", "support",
        "adore", "appreciate", "care about", "respect", "value",
    )

    private fun String.capitalizeFirstLetter(): String {
        require(this.isNotBlank())
        return this.first().uppercase() + this.substring(1)
    }

    fun dummyTitle(member: String = Members.all.random()): String {
        return "${member.capitalizeFirstLetter()} is " +
                adjectives.random().capitalizeFirstLetter()
    }

    fun dummyAuthor(): String {
        return "${nouns.random().capitalizeFirstLetter()}${
            adjectives.random().capitalizeFirstLetter()
        }-${Ids.random(3)}"
    }

    fun dummyContent(member: String = Members.all.random()): String {
        return "I ${verbs.random()} $member so much."
    }
}
