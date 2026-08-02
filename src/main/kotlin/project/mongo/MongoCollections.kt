package project.mongo

/**
 * Encompasses the name of mongo database collection for the 4 base collections.
 */
data class MongoCollections(
    // core collection
    val userAccount: String,
    val serverObjects: String,

    // domain server collection
    val websiteSession: String,

    // domain collection
    val topic: String,
    val reply: String,
    val spaces: String,
    val sections: String
)

val RuntimeMongoCollections = MongoCollections(
    userAccount = "user_account",
    serverObjects = "server_objects",

    websiteSession = "website_session",

    topic = "topic",
    reply = "reply",
    spaces = "spaces",
    sections = "sections"
)
