package project.domain.profile

import encore.datastore.collection.UserId

class BlankProfileRepository: ProfileRepository {
    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        TODO("Not yet implemented")
    }
}
