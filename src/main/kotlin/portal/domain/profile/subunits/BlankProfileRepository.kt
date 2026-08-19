package portal.domain.profile.subunits

import portal.domain.profile.model.Profile
import portal.mongo.collection.UserId

class BlankProfileRepository: ProfileRepository {
    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserSummary(userId: UserId): Result<UserSummary?> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserSummaries(userIds: List<UserId>): Result<Map<UserId, UserSummary>> {
        TODO("Not yet implemented")
    }
}
