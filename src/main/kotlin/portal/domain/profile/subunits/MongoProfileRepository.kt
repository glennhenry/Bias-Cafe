package portal.domain.profile.subunits

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.account.FieldUserId
import encore.datastore.runMongoCatching
import encore.utils.toJsonString
import kotlinx.coroutines.flow.associateBy
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId
import portal.domain.profile.model.Profile
import portal.mongo.collection.UserAccount
import portal.mongo.collection.UserId

/** `displayName`*/
val FieldDisplayName = Profile::displayName.name

/** `avatarUrl`*/
val FieldAvatarUrl = Profile::avatarUrl.name

class MongoProfileRepository(
    private val profiles: MongoCollection<Profile>
) : ProfileRepository {
    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        return runMongoCatching {
            profiles
                .find(Filters.eq(FieldUserId, userId))
                .firstOrNull()
        }
    }

    override suspend fun getUserSummary(userId: UserId): Result<UserSummary?> {
        return runMongoCatching {
            val query = profiles
                .withDocumentClass<QueryUserSummary>()
                .find(Filters.eq(FieldUserId, userId))
                .projection(
                    Projections.fields(
                        Projections.excludeId(),
                        Projections.include(FieldUserId),
                        Projections.include(FieldDisplayName),
                        Projections.include(FieldAvatarUrl)
                    )
                )
                .firstOrNull()

            return if (query != null) {
                Result.success(UserSummary(query.userId, query.displayName, query.avatarUrl))
            } else {
                Result.success(null)
            }
        }
    }

    override suspend fun getUserSummaries(userIds: List<UserId>): Result<Map<UserId, UserSummary>> {
        return runMongoCatching {
            val profile = profiles.aggregate<QueryUserSummary>(
                listOf(
                    Aggregates.match(Filters.`in`(FieldUserId, userIds)),
                    Aggregates.project(
                        Projections.fields(
                            Projections.excludeId(),
                            Projections.include(FieldUserId),
                            Projections.include(FieldDisplayName),
                            Projections.include(FieldAvatarUrl)
                        )
                    )
                )
            ).associateBy(
                keySelector = { it.userId },
                valueTransform = { UserSummary(it.userId, it.displayName, it.avatarUrl) }
            )

            return Result.success(profile)
        }
    }
}

/**
 * Mongo projection class to query various field of [UserAccount] and [Profile].
 */
data class QueryUserSummary(
    @field:BsonId val id: String? = null,
    val userId: UserId,
    val displayName: String,
    val avatarUrl: String
)
