package encore.extra

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.account.FieldUserId
import encore.datastore.runMongoCatching
import encore.datastore.throwIfNothingMatched
import kotlinx.coroutines.flow.firstOrNull
import project.mongo.collection.UserAccount
import project.mongo.collection.UserId

class MongoUserExtraRepository(
    private val accCollection: MongoCollection<UserAccount>
) : UserExtraRepository {
    override suspend fun getExtra(userId: UserId, key: String): Result<String?> {
        return runMongoCatching {
            accCollection
                .find(Filters.eq(FieldUserId, userId))
                .firstOrNull()
                ?.extra[key]
        }
    }

    override suspend fun getAllExtra(userId: UserId, key: String): Result<Map<String, String>?> {
        return runMongoCatching {
            accCollection
                .find(Filters.eq(FieldUserId, userId))
                .firstOrNull()
                ?.extra
        }
    }

    override suspend fun updateExtra(
        userId: UserId, key: String, value: String
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldUserId, userId)
            val update = Updates.set("extra.$key", value)
            accCollection.updateOne(filter, update)
                .throwIfNothingMatched("updateExtra") { filter }
        }
    }

    override suspend fun deleteExtra(
        userId: UserId,
        key: String
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldUserId, userId)
            val update = Updates.unset("extra.$key")
            accCollection.updateOne(filter, update)
                .throwIfNothingMatched("deleteExtra") { filter }
        }
    }
}
