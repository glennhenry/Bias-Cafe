package project.dummy

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.fancam.Fancam
import encore.venue.Venue
import project.context.ServerContext
import project.domain.cafe.topic.Topic
import project.mongo.RuntimeMongoCollections
import kotlin.random.Random

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
     * 1. Create 15-20 dummy accounts with random username and email
     *    produced by [AccountFactory], and password fixed to "dummy".
     * 2. Create 0-5 posts for each previously created dummy accounts;
     *    20% chance for 0 post for an account.
     * 3. Create 0-6 replies for each posts; 30% chance for 0 reply.
     * 4. Create 0-4 comments for each replies; 50% chance for 0 comment.
     */
    suspend fun setup() {
        val numAccounts = (15..20).random()
        val numTopicsThresholdToDummy = 5

        val shouldDummy = mongoDatabase
            .getCollection<Topic>(RuntimeMongoCollections.topic)
            .estimatedDocumentCount() < numTopicsThresholdToDummy

        if (Venue.custom.setupDummyActivity && shouldDummy) {
            val insertedUsers = mutableListOf<String>()
            val addedTopics = mutableListOf<Pair<String, Long>>()

            try {
                // 1. create accounts
                repeat(numAccounts) {
                    val username = AccountFactory.username()
                    val userId = serverContext.subunits.creation.createUser(
                        username = username,
                        password = "dummy",
                        email = AccountFactory.email()
                    )
                    insertedUsers.add(userId)
                }

                // 2. create topics
                insertedUsers.forEach { userId ->
                    // 80% chance of post, 20% chance of no post
                    if (Random.nextDouble() < 0.8) {
                        val numPostsEachAccounts = (0..5).random()
                        val topics = TopicFactory.topics(userId, numPostsEachAccounts)
                        for (topic in topics) {
                            serverContext.subunits.topic.addTopic(topic)
                            addedTopics.add(topic.topicId to topic.postedDate)
                        }
                    }
                }

                // 3. create replies and comments
                addedTopics.forEach { (topicId, postedDate) ->
                    // 70% chance of reply, 30% chance for no reply
                    if (Random.nextDouble() < 0.7) {
                        val amountOfReply = (1..6).random()
                        val replies = List(amountOfReply) {
                            ReplyFactory.reply(
                                topicId = topicId,
                                topicPostDate = postedDate,
                                possibleReplyAuthors = insertedUsers,
                                possibleAmountofComments = 1..4,
                                possibleCommentAuthors = insertedUsers
                            )
                        }
                        replies.forEach { serverContext.subunits.reply.addReply(it) }
                    }
                }
            } catch (e: Exception) {
                Fancam.error(e, "dummysetup") { "Error during dummy setup" }
            }
            Fancam.info { "Dummy setup completed." }
        }
    }
}
