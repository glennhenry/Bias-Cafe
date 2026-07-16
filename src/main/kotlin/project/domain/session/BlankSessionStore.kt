package project.domain.session

class BlankSessionStore: SessionStore {
    override suspend fun load(): Result<List<SessionStoreModel>> {
        TODO("Not yet implemented")
    }

    override suspend fun put(
        userId: String,
        token: String,
        expiresAt: Long
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun update(token: String, expiresAt: Long): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(token: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun batchDeleteExpiredSessions(currentTime: Long): Result<Unit> {
        TODO("Not yet implemented")
    }
}
