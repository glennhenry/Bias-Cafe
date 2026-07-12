package project.domain.session

/**
 * Represent a persistent session storage.
 *
 * Implementation is responsible for retrival and update of session identifiers
 * from a persistent component (e.g., MongoDB).
 */
interface SessionStore {
    /**
     * Load every available session in the store.
     * @return List of the session which is a pair of `token` to `expiresAt`.
     *         Use `Result.failure` for internal store error,
     *         otherwise wrap in `Result.success`.
     */
    suspend fun load(): Result<List<SessionStoreModel>>

    /**
     * Put the session identified with [token] that expires at [expiresAt].
     * @return `Result.failure` for internal store error, otherwise `Result.success`.
     */
    suspend fun put(token: String, expiresAt: Long): Result<Unit>

    /**
     * Update the session identified with [token] with a new [expiresAt].
     * @return `Result.failure` for internal store error, otherwise `Result.success`.
     */
    suspend fun update(token: String, expiresAt: Long): Result<Unit>

    /**
     * Delete the session identified by [token].
     * @return `Result.failure` for internal store error, otherwise `Result.success`.
     */
    suspend fun delete(token: String): Result<Unit>

    /**
     * Delete all expired sessions, which is identified when `expiresAt` is more than [currentTime].
     * @return `Result.failure` for internal store error, otherwise `Result.success`.
     */
    suspend fun batchDeleteExpiredSessions(currentTime: Long): Result<Unit>
}
