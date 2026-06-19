package project.domain.profile

import encore.datastore.collection.Profile
import encore.datastore.collection.UserId

interface ProfileRepository {
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
}
