package encore.account

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.*
import project.mongo.collection.UserAccount
import project.mongo.collection.UserId
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId

/** `userId`*/
val FieldUserId = UserAccount::userId.name

/** `username`*/
val FieldUsername = UserAccount::username.name

/** `email`*/
val FieldEmail = UserAccount::email.name

/** `hashedPassword`*/
val FieldPassword = UserAccount::hashedPassword.name

/** `profile.lastActiveAt`*/
val FieldLastActive = UserAccount::lastActiveAt.name

/**
 * [AccountRepository] implementation using MongoDB.
 */
class MongoAccountRepository(val accountCollection: MongoCollection<UserAccount>) : AccountRepository {
    override suspend fun getAccountByUserId(userId: String): Result<UserAccount?> {
        return runMongoCatching {
            accountCollection
                .find(Filters.eq(FieldUserId, userId))
                .firstOrNull()
        }
    }

    override suspend fun getAccountByUsername(username: String): Result<UserAccount?> {
        return runMongoCatching {
            accountCollection
                .find(Filters.eq(FieldUsername, username))
                .firstOrNull()
        }
    }

    override suspend fun getUserIdByUsername(username: String): Result<UserId> {
        return runMongoCatching {
            accountCollection
                .withDocumentClass<QueryUserId>()
                .find(Filters.eq(FieldUsername, username))
                .projection(
                    Projections.fields(
                        Projections.include(FieldUserId),
                        Projections.excludeId()
                    )
                )
                .firstOrNull()
                ?.userId
        }
    }

    override suspend fun getCredentials(username: String): Result<Credentials?> {
        return runMongoCatching {
            val account = accountCollection
                .withDocumentClass<QueryCredentials>()
                .find(Filters.eq(FieldUsername, username))
                .projection(Projections.include(FieldPassword, FieldUserId))
                .firstOrNull()

            if (account == null) {
                return Result.success(null)
            }

            val userId = account.userId
            val hashedPassword = account.hashedPassword
            return Result.success(Credentials(userId, hashedPassword))
        }
    }

    override suspend fun updateUserAccount(
        userId: UserId,
        account: UserAccount
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldUserId, userId)
            accountCollection
                .replaceOne(filter, account)
                .throwIfNothingMatched("updateUserAccount not updated for $userId", { filter })
        }
    }

    override suspend fun updateLastActivity(
        userId: UserId,
        lastActivity: Long
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldUserId, userId)
            val update = Updates.set(FieldLastActive, lastActivity)
            accountCollection
                .updateOne(filter, update)
                .throwIfNothingMatched("updateLastActivity not updated for $userId", { filter })
        }
    }

    override suspend fun usernameExists(username: String): Result<Boolean> {
        return runMongoCatching {
            accountCollection
                .find(Filters.eq(FieldUsername, username))
                .projection(null)
                .firstOrNull() != null
        }
    }

    override suspend fun emailExists(email: String): Result<Boolean> {
        return runMongoCatching {
            accountCollection
                .find(Filters.eq(FieldEmail, email))
                .projection(null)
                .firstOrNull() != null
        }
    }
}

/**
 * Mongo projection class to query the `userId` of [UserAccount].
 */
data class QueryUserId(
    @field:BsonId val id: String? = null,
    val userId: UserId
)

/**
 * Mongo projection class to query the `userId` and `hashedPassword` of [UserAccount].
 */
data class QueryCredentials(
    @field:BsonId val id: String? = null,
    val userId: UserId,
    val hashedPassword: String
)
