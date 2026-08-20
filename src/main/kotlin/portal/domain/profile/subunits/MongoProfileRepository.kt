package portal.domain.profile.subunits

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.account.FieldUserId
import encore.datastore.runMongoCatching
import encore.utils.support.asUnit
import kotlinx.coroutines.flow.associateBy
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId
import portal.domain.profile.model.FanProfile
import portal.domain.profile.model.FanProfileSummary
import portal.domain.profile.model.Profile
import portal.domain.profile.model.OverviewSummary
import portal.mongo.collection.UserAccount
import portal.mongo.collection.UserId
import kotlin.String
import kotlin.collections.List

/** `displayName`*/
val FieldDisplayName = Profile::displayName.name

/** `avatarUrl`*/
val FieldAvatarUrl = Profile::avatarUrl.name

class MongoProfileRepository(
    private val profiles: MongoCollection<Profile>
) : ProfileRepository {
    override suspend fun insert(profile: Profile): Result<Unit> {
        return runMongoCatching {
            profiles.insertOne(profile).asUnit()
        }
    }

    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        return runMongoCatching {
            profiles
                .find(Filters.eq(FieldUserId, userId))
                .firstOrNull()
        }
    }

    override suspend fun getProfileOverview(userId: UserId): Result<OverviewSummary?> {
        return runMongoCatching {
            val query = profiles
                .withDocumentClass<QueryProfileOverview>()
                .find(Filters.eq(FieldUserId, userId))
                .projection(
                    Projections.fields(
                        Projections.excludeId(),
                        Projections.include(FieldDisplayName),
                        Projections.include(FieldAvatarUrl),
                        Projections.include("country"),
                        Projections.include("birthday"),
                        Projections.include("bio")
                    )
                )
                .firstOrNull()

            return if (query != null) {
                Result.success(
                    OverviewSummary(
                        query.displayName,
                        query.avatarUrl,
                        query.country,
                        query.birthday,
                        query.bio
                    )
                )
            } else {
                Result.success(null)
            }
        }
    }

    override suspend fun getFanProfile(userId: UserId): Result<FanProfileSummary?> {
        return runMongoCatching {
            val query = profiles
                .withDocumentClass<QueryFanProfile>()
                .find(Filters.eq(FieldUserId, userId))
                .projection(
                    Projections.fields(
                        Projections.excludeId(),
                        Projections.include(FieldDisplayName),
                        Projections.include(FieldAvatarUrl),
                        Projections.computed("bias", $$"$fanProfile.bias"),
                        Projections.computed("favoriteSong", $$"$fanProfile.favoriteSong"),
                        Projections.computed("favoriteEra", $$"$fanProfile.favoriteEra"),
                        Projections.computed("story", $$"$fanProfile.story")
                    )
                ).firstOrNull()

            return if (query != null) {
                Result.success(
                    FanProfileSummary(
                        query.displayName,
                        query.avatarUrl,
                        query.bias,
                        query.favoriteSong,
                        query.favoriteEra,
                        query.story
                    )
                )
            } else {
                Result.success(null)
            }
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

/**
 * Mongo projection class to query various field of [Profile].
 */
data class QueryProfileOverview(
    @field:BsonId val id: String? = null,
    val displayName: String,
    val avatarUrl: String,
    val country: String,
    val birthday: String,
    val bio: String
)

/**
 * Mongo projection class to query `displayName`, `avatarUrl`, and
 * [FanProfile] field of [Profile.fanProfile].
 */
data class QueryFanProfile(
    @field:BsonId val id: String? = null,
    val displayName: String,
    val avatarUrl: String,
    val bias: List<String>,
    val favoriteSong: String,
    val favoriteEra: String,
    val story: String
)
