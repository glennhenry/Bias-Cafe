import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.datastore.MongoCollectionName
import org.bson.Document

const val PLANET_NETWORK_TEST_DB_NAME = "PLANET_NETWORK-test-DB"
const val MONGO_TEST_URL = "mongodb://localhost:27017"
val TestMongoCollectionName = MongoCollectionName(
    playerAccount = "test_player_account",
    playerObjects = "test_player_objects",
    playerServerObjects = "test_player_server_objects",
    serverObjects = "test_server_objects"
)

suspend fun initMongo(
    dbUrl: String = MONGO_TEST_URL,
    dbName: String = PLANET_NETWORK_TEST_DB_NAME
): MongoDatabase {
    val mongoc = MongoClient.create(dbUrl)
    val db = mongoc.getDatabase(dbName)
    db.runCommand(Document("ping", 1))
    return db
}
