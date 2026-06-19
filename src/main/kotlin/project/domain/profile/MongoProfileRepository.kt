package project.domain.profile

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.FieldUserId
import encore.datastore.collection.Profile
import encore.datastore.collection.UserId
import encore.datastore.runMongoCatching
import kotlinx.coroutines.flow.firstOrNull

class MongoProfileRepository(private val profileCollection: MongoCollection<Profile>): ProfileRepository {
    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        return runMongoCatching {
            profileCollection
                .find(Filters.eq(FieldUserId, userId))
                .firstOrNull()
        }
    }
}
