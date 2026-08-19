import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.Document
import portal.mongo.MongoCollections

const val BIAS_CAFE_TEST_DB_NAME = "BIAS_CAFE-test-DB"
const val MONGO_TEST_URL = "mongodb://localhost:27017"
val TestCollections = MongoCollections(
    userAccount = "test_user_account",
    serverObjects = "test_server_objects",

    websiteSession = "test_website_session",

    profiles = "test_profiles",
    topic = "test_topic",
    reply = "test_reply",
    spaces = "test_spaces",
    sections = "test_sections"
)

suspend fun initMongo(
    dbUrl: String = MONGO_TEST_URL,
    dbName: String = BIAS_CAFE_TEST_DB_NAME
): MongoDatabase {
    val mongoc = MongoClient.create(dbUrl)
    val db = mongoc.getDatabase(dbName)
    db.runCommand(Document("ping", 1))
    return db
}
