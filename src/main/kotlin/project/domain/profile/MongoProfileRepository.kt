package project.domain.profile

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.account.FieldUserId
import encore.datastore.runMongoCatching
import kotlinx.coroutines.flow.associateBy
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId
import project.mongo.collection.UserAccount
import project.mongo.collection.UserId

/** `profile`*/
val FieldProfile = UserAccount::profile.name

/** `displayName`*/
val FieldDisplayName = Profile::displayName.name

/** `avatarUrl`*/
val FieldAvatarUrl = Profile::avatarUrl.name

class MongoProfileRepository(
    private val accountCollection: MongoCollection<UserAccount>
) : ProfileRepository {
    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        return runMongoCatching {
            val account = accountCollection
                .withDocumentClass<QueryProfile>()
                .find(Filters.eq(FieldUserId, userId))
                .projection(
                    Projections.fields(
                        Projections.include(FieldProfile),
                        Projections.excludeId()
                    )
                )
                .firstOrNull()

            if (account == null) {
                return Result.success(null)
            }

            return Result.success(account.profile)
        }
    }

    override suspend fun getUserSummary(userId: UserId): Result<UserSummary?> {
        return runMongoCatching {
            val account = accountCollection.aggregate<QueryUserSummary>(
                listOf(
                    Aggregates.match(Filters.eq(FieldUserId, userId)),
                    Aggregates.project(
                        Projections.fields(
                            Projections.excludeId(),
                            Projections.include(FieldUserId),
                            Projections.computed(FieldDisplayName, $$"$profile.displayName"),
                            Projections.computed(FieldAvatarUrl, $$"$profile.avatarUrl")
                        )
                    )
                )
            ).firstOrNull()

            if (account == null) {
                return Result.success(null)
            }

            return Result.success(UserSummary(account.userId, account.displayName, account.avatarUrl))
        }
    }

    override suspend fun getUserSummaries(userIds: List<UserId>): Result<Map<UserId, UserSummary>> {
        return runMongoCatching {
            val account = accountCollection.aggregate<QueryUserSummary>(
                listOf(
                    Aggregates.match(Filters.`in`(FieldUserId, userIds)),
                    Aggregates.project(
                        Projections.fields(
                            Projections.excludeId(),
                            Projections.include(FieldUserId),
                            Projections.computed(FieldDisplayName, $$"$profile.displayName"),
                            Projections.computed(FieldAvatarUrl, $$"$profile.avatarUrl")
                        )
                    )
                )
            ).associateBy(
                keySelector = { it.userId },
                valueTransform = { UserSummary(it.userId, it.displayName, it.avatarUrl) }
            )

            return Result.success(account)
        }
    }
}

/**
 * Mongo projection class to query the `profile` of [UserAccount].
 */
data class QueryProfile(
    @field:BsonId val id: String? = null,
    val profile: Profile
)

/**
 * Mongo projection class to query various field of [UserAccount] and [Profile].
 */
data class QueryUserSummary(
    @field:BsonId val id: String? = null,
    val userId: UserId,
    val displayName: String,
    val avatarUrl: String
)
