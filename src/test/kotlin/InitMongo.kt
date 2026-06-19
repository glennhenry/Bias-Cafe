import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.datastore.MongoCollectionName
import org.bson.Document

const val BIAS_CAFE_TEST_DB_NAME = "BIAS_CAFE-test-DB"
const val MONGO_TEST_URL = "mongodb://localhost:27017"
val TestMongoCollectionName = MongoCollectionName(
    userAccount = "test_user_account",
    profile = "test_profile",
    serverObjects = "test_server_objects"
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
