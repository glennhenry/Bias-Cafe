package project.domain

import encore.datastore.collection.Profile
import encore.datastore.collection.UserId

class BlankProfileRepository: ProfileRepository {
    override suspend fun getProfile(userId: UserId): Result<Profile?> = TODO("NO OPERATION")
}
