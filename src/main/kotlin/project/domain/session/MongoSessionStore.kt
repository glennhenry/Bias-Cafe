package project.domain.session

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.ensureAck
import encore.datastore.runMongoCatching
import encore.datastore.throwIfNotModified
import encore.utils.support.asUnit
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable

val FieldToken = SessionStoreModel::token.name
val FieldExpiresAt = SessionStoreModel::expiresAt.name

class MongoSessionStore(private val sessionCollection: MongoCollection<SessionStoreModel>) : SessionStore {
    override suspend fun load(): Result<List<SessionStoreModel>> {
        return runMongoCatching {
            sessionCollection.find().toList()
        }
    }

    override suspend fun put(userId: String, token: String, expiresAt: Long): Result<Unit> {
        return runMongoCatching {
            ensureAck(sessionCollection.insertOne(SessionStoreModel(userId, token, expiresAt)))
                .asUnit()
        }
    }

    override suspend fun update(token: String, expiresAt: Long): Result<Unit> {
        val filter = Filters.eq(FieldToken, token)
        val update = Updates.set(FieldExpiresAt, expiresAt)
        return runMongoCatching {
            sessionCollection.updateOne(filter, update)
                .throwIfNotModified("MongoSessionStore update", { filter }, { update })
        }
    }

    override suspend fun delete(token: String): Result<Unit> {
        return runMongoCatching {
            if (ensureAck(sessionCollection.deleteOne(Filters.eq(FieldToken, token))).deletedCount <= 0) {
                error("$token wasn't deleted (deletedCount <= 0)")
            }
        }
    }

    override suspend fun batchDeleteExpiredSessions(currentTime: Long): Result<Unit> {
        val filter = Filters.lte(FieldExpiresAt, currentTime)
        return runMongoCatching {
            ensureAck(sessionCollection.deleteMany(filter)).asUnit()
        }
    }
}

@Serializable
data class SessionStoreModel(
    val userId: String,
    val token: String,
    val expiresAt: Long
)
