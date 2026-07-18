package project.domain.profile

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.FieldUserId
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.datastore.runMongoCatching
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId

val FieldProfile = UserAccount::profile.name

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
}

/**
 * Mongo projection class to query the `profile` of [UserAccount].
 */
data class QueryProfile(
    @field:BsonId val id: String? = null,
    val profile: Profile
)
