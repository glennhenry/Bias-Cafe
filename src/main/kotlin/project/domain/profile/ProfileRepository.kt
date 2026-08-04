package project.domain.profile

import project.mongo.collection.UserId

interface ProfileRepository {
    /**
     * Returns [Profile] associated with the given [userId], if it exists.
     *
     * This basically only query the profile field of the user's account.
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
     * to a nullable [UserSummary].
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getUserSummaries(userIds: List<UserId>): Result<Map<UserId, UserSummary?>>
}
