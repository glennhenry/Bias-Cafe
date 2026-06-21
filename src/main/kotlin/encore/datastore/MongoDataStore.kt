package encore.datastore

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.datastore.collection.Profile
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.datastore.collection.ServerObjects
import encore.fancam.Fancam
import encore.fancam.Tags
import encore.utils.support.asUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.measureTime

/**
 * Encompasses the name of mongo database collection for the 4 base collections.
 */
data class MongoCollectionName(
    val userAccount: String,
    val serverObjects: String,

    val profile: String,
    val topic: String
)

/**
 * Implementation of [DataStore] with Kotlin MongoDB coroutine driver.
 *
 * The four core collections are implemented as one collection each.
 * Separating data per-domain to different collections may result better for
 * scalability and performance. However, for simplicity, data is unified
 * to reduce domain modelling decision and to keep implementation faster to write.
 */
class MongoDataStore(db: MongoDatabase, collectionName: MongoCollectionName) : DataStore {
    private val accounts = db.getCollection<UserAccount>(collectionName.userAccount)
    private val profiles = db.getCollection<Profile>(collectionName.profile)
    private val serverObjects = db.getCollection<ServerObjects>(collectionName.serverObjects)

    private val initJob = CoroutineScope(Dispatchers.IO).async { setupCollections() }

    override suspend fun awaitInit() {
        Fancam.info(Tags.Datastore) { "Waiting for MongoDB initialization..." }
        val elapsed = measureTime {
            initJob.await()
        }
        Fancam.info(Tags.Datastore) { "MongoDB initialized in ${elapsed}ms" }
    }

    private suspend fun setupCollections() {
        try {
            val count = accounts.estimatedDocumentCount()
            Fancam.info(Tags.Datastore) { "MongoDB contains $count accounts" }
            prepareServerObjects()
            setupIndexes()
        } catch (e: Exception) {
            Fancam.error(e, Tags.Datastore) { "MongoDB scandal during initialization" }
        }
    }

    private suspend fun setupIndexes() {
        serverObjects.createIndex(Indexes.text())
        Fancam.info(Tags.Datastore) { "Mongo index set up" }
    }

    private suspend fun prepareServerObjects() {
        when (val count = serverObjects.estimatedDocumentCount()) {
            0L -> {
                serverObjects.insertOne(ServerObjects())
            }

            1L -> return

            else -> {
                Fancam.warn(Tags.Datastore) { "Detected multiple server object document count=$count" }
            }
        }
    }

    override suspend fun userExists(userId: UserId): Boolean {
        return accounts.find(Filters.eq(FieldUserId, userId)).firstOrNull() != null
    }

    override suspend fun getUserAccount(userId: UserId): UserAccount? {
        return accounts.find(Filters.eq(FieldUserId, userId)).firstOrNull()
    }

    override suspend fun getServerObjects(): ServerObjects {
        return serverObjects.find(ServerObjectsFilter).firstOrNull()
            ?: throw NoSuchElementException("ServerObjects not found, please ensure ServerObjects creation.")
    }

    override suspend fun create(account: UserAccount, profile: Profile): Result<Unit> {
        return runMongoCatching {
            ensureAck(accounts.insertOne(account))
                .and(profiles.insertOne(profile))
                .asUnit()
        }
    }

    override suspend fun delete(userId: UserId): Result<Unit> {
        return runMongoCatching {
            ensureAck(accounts.deleteOne(Filters.eq(FieldUserId, userId)))
                .and(profiles.deleteOne(Filters.eq(FieldUserId, userId)))
                .asUnit()
        }
    }

    override suspend fun shutdown() = Unit
}
