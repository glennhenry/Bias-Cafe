package portal.domain.profile

import portal.mongo.collection.UserId
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.utils.types.Outcome
import encore.utils.types.toOutcome
import portal.utils.peek

class ProfileSubunit(private val profileRepository: ProfileRepository) : Subunit<ServerScope> {
    /**
     * Returns an [Outcome] containing [Profile] associated with [userId].
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with `null` if account does not exist.
     * - [Outcome.Ok] with the `profile` otherwise.
     */
    suspend fun getProfile(userId: UserId): Outcome<Profile?> {
        return profileRepository.getProfile(userId)
            .onFailure {
                Fancam.error(it, "profile") {
                    "getProfile failed: repository scandal for '$userId'"
                }
            }
            .toOutcome { profile -> return Outcome.Ok(profile) }
    }

    /**
     * Returns an [Outcome] containing [UserSummary] of [userId].
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with `null` if the user does not exist.
     * - [Outcome.Ok] with the summary otherwise.
     */
    suspend fun getUserSummary(userId: UserId): Outcome<UserSummary?> {
        return profileRepository.getUserSummary(userId)
            .onFailure {
                Fancam.error(it, "profile") {
                    "getUserSummary failed: repository scandal for '$userId'"
                }
            }
            .toOutcome { profile -> return Outcome.Ok(profile) }
    }

    /**
     * Returns an [Outcome] containing a map of each `userId` in [userIds] to [UserSummary].
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the map otherwise.
     */
    suspend fun getUserSummaries(userIds: List<UserId>): Outcome<Map<UserId, UserSummary>> {
        return profileRepository.getUserSummaries(userIds)
            .onFailure {
                Fancam.error(it, "profile") {
                    "getUserSummaries failed: repository scandal while querying for ${userIds.peek(3).joinToString()}"
                }
            }
            .toOutcome { profile -> return Outcome.Ok(profile) }
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> {
        return runCatching { }
    }

    override suspend fun disband(scope: ServerScope): Result<Unit> {
        return runCatching { }
    }

    companion object {
        /**
         * Creates a test instance of [ProfileSubunit].
         *
         * @param profileRepository use [BlankProfileRepository] when not under test.
         */
        fun createForTest(
            profileRepository: ProfileRepository = BlankProfileRepository()
        ): ProfileSubunit {
            return ProfileSubunit(profileRepository)
        }
    }
}
