package portal.domain.profile.subunits

import portal.domain.profile.model.FanProfile
import portal.domain.profile.model.FanProfileSummary
import portal.domain.profile.model.Profile
import portal.domain.profile.model.OverviewSummary
import portal.mongo.collection.UserId

interface ProfileRepository {
    /**
     * Insert a new profile.
     * @return [Result] type denoting success or failure.
     */
    suspend fun insert(profile: Profile): Result<Unit>

    /**
     * Returns [Profile] associated with the given [userId], if it exists.
     *
     * Returns [Result.success] with:
     * - the [Profile] if found
     * - `null` if no account exists for the given [userId]
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getProfile(userId: UserId): Result<Profile?>

    /**
     * Returns [OverviewSummary] of [userId], if it exists.
     * This queries the overview information of user's [Profile].
     *
     * Returns [Result.success] with:
     * - [OverviewSummary] if the user is found
     * - `null` if no profile exists for the given [userId]
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getProfileOverview(userId: UserId): Result<OverviewSummary?>

    /**
     * Returns [FanProfileSummary] of [userId], if it exists.
     * This queries the fan profile information of user's [Profile.fanProfile].
     *
     * Returns [Result.success] with:
     * - [FanProfileSummary] if the user is found
     * - `null` if no profile exists for the given [userId]
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getFanProfile(userId: UserId): Result<FanProfileSummary?>

    /**
     * Returns [UserSummary] of user by its [userId], if it exists.
     *
     * Returns [Result.success] with:
     * - the [UserSummary] if found
     * - `null` if no user exists for the given [userId]
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getUserSummary(userId: UserId): Result<UserSummary?>

    /**
     * Returns the [UserSummary] of all users identified by `userId`
     * in the [userIds] list.
     *
     * Returns [Result.success] with a map of each `userId`
     * to the [UserSummary]. If `userId` is not available in the map, it means
     * the id is not found.
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getUserSummaries(userIds: List<UserId>): Result<Map<UserId, UserSummary>>
}
