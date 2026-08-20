package portal.domain.profile.subunits

import portal.domain.profile.model.FanProfile
import portal.domain.profile.model.FanProfileSummary
import portal.domain.profile.model.Profile
import portal.domain.profile.model.OverviewSummary
import portal.mongo.collection.UserId

class BlankProfileRepository: ProfileRepository {
    override suspend fun insert(profile: Profile): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getProfile(userId: UserId): Result<Profile?> {
        TODO("Not yet implemented")
    }

    override suspend fun getProfileOverview(userId: UserId): Result<OverviewSummary?> {
        TODO("Not yet implemented")
    }

    override suspend fun getFanProfile(userId: UserId): Result<FanProfileSummary?> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserSummary(userId: UserId): Result<UserSummary?> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserSummaries(userIds: List<UserId>): Result<Map<UserId, UserSummary>> {
        TODO("Not yet implemented")
    }
}
