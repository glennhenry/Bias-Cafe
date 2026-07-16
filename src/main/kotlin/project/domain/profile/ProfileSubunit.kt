package project.domain.profile

import encore.datastore.collection.Profile
import encore.datastore.collection.UserId
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.utils.types.Outcome
import encore.utils.types.Report
import encore.utils.types.toOutcome
import encore.utils.types.toReport

class ProfileSubunit(private val profileRepository: ProfileRepository) : Subunit<ServerScope> {
    /**
     * Returns an [Outcome] containing [Profile] associated with [userId].
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with `null` if account does not exist.
     * - [Outcome.Ok] with the `profile` otherwise.
     */
    suspend fun createProfile(userId: UserId, profile: Profile): Report {
        return profileRepository.createProfile(userId, profile)
            .onFailure {
                Fancam.error(it, "profile") {
                    "createProfile failed: repository scandal for '$userId' on profile=$profile"
                }
            }
            .toReport()
    }

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
