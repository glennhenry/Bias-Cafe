package project.dummy

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.fancam.Fancam
import encore.venue.Venue
import project.context.ServerContext
import project.domain.cafe.topic.Topic
import project.mongo.RuntimeMongoCollections

/**
 * Component to prepare dummy activites for the website.
 *
 * For testing purposes, this class can create dummy accounts, topic posts,
 * comments, replies, and other activity around the website.
 */
class DummyActivitySetup(
    private val mongoDatabase: MongoDatabase,
    private val serverContext: ServerContext
) {
    /**
     * If the topics are less than 5:
     *
     * Setup:
     * 1. Create 10-15 dummy accounts through with random username and email
     *    produced by [AccountFactory], and password fixed to "dummy".
     * 2. Create 5-10 posts for each previously created dummy accounts.
     */
    suspend fun setup() {
        val numAccounts = (10..15).random()
        val numTopicsThresholdToDummy = 5

        val shouldDummy = mongoDatabase
            .getCollection<Topic>(RuntimeMongoCollections.topic)
            .estimatedDocumentCount() < numTopicsThresholdToDummy

        if (Venue.custom.setupDummyActivity && shouldDummy) {
            val insertedUsers = mutableListOf<String>()
            try {
                repeat(numAccounts) {
                    val username = AccountFactory.username()
                    val userId = serverContext.subunits.creation.createUser(
                        username = username,
                        password = "dummy",
                        email = AccountFactory.email()
                    )
                    insertedUsers.add(userId)
                }

                insertedUsers.forEach { userId ->
                    val numPostsEachAccounts = (5..10).random()
                    val topics = TopicFactory.topics(userId, numPostsEachAccounts)
                    for (topic in topics) {
                        serverContext.subunits.topic.addTopic(topic)
                    }
                }
            } catch (e: Exception) {
                Fancam.error(e, "dummysetup") { "Error during dummy setup" }
            }
            Fancam.info { "Dummy setup completed." }
        }
    }
}
