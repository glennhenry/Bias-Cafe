package encore.account

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.account.model.Credentials
import encore.account.model.Profile
import encore.datastore.*
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId

/**
 * [AccountRepository] implementation using MongoDB.
 */
class MongoAccountRepository(val accountCollection: MongoCollection<UserAccount>) : AccountRepository {
    override suspend fun getAccountByUsername(username: String): Result<UserAccount> {
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

    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        return runMongoCatching {
            accountCollection
                .withDocumentClass<QueryProfile>()
                .find(Filters.eq(FieldUserId, userId))
                .projection(Projections.include(FieldProfile))
                .firstOrNull()
                ?.profile
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
                .throwIfNotModified("updateUserAccount not updated for $userId", { filter }, null)
        }
    }

    override suspend fun updateProfile(
        userId: UserId,
        profile: Profile
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldUserId, userId)
            val update = Updates.set(FieldProfile, profile)
            accountCollection
                .updateOne(filter, update)
                .throwIfNotModified("updateProfile not updated for $userId", { filter }, { update })
        }
    }

    override suspend fun updateLastActivity(
        userId: UserId,
        lastActivity: Long
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldUserId, userId)
            val update = Updates.set(FieldProfileLastActive, lastActivity)
            accountCollection
                .updateOne(filter, update)
                .throwIfNotModified("updateLastActivity not updated for $userId", { filter }, { update })
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

/**
 * Mongo projection class to query the `profile` of [UserAccount].
 */
data class QueryProfile(
    @field:BsonId val id: String? = null,
    val profile: Profile
)
