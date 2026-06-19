package project.domain

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.FieldUserId
import encore.datastore.collection.Profile
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.datastore.runMongoCatching
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId

class MongoProfileRepository(private val profileCollection: MongoCollection<Profile>): ProfileRepository {
    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        return runMongoCatching {
            profileCollection
                .find(Filters.eq(FieldUserId, userId))
                .firstOrNull()
        }
    }
}
