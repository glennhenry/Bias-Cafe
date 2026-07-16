package project.domain.profile

import encore.datastore.collection.Profile
import encore.datastore.collection.UserId

class BlankProfileRepository: ProfileRepository {
    override suspend fun createProfile(
        userId: String,
        profile: Profile
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getProfile(userId: UserId): Result<Profile?> = TODO("NO OPERATION")
}
