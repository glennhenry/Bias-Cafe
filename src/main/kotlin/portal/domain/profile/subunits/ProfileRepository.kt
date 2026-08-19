package portal.domain.profile.subunits

import portal.domain.profile.model.Profile
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
