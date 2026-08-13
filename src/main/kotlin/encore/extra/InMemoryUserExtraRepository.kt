package encore.extra

import portal.mongo.collection.UserId

class InMemoryUserExtraRepository(
    private val initialMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
) : UserExtraRepository {
    override suspend fun getExtra(
        userId: UserId,
        key: String
    ): Result<String?> {
        return Result.success(initialMap[userId]?.get(key))
    }

    override suspend fun getAllExtra(
        userId: UserId,
        key: String
    ): Result<Map<String, String>?> {
        return Result.success(initialMap[userId])
    }

    override suspend fun updateExtra(
        userId: UserId,
        key: String,
        value: String
    ): Result<Unit> {
        initialMap[userId]?.set(key, value)
        return Result.success(Unit)
    }

    override suspend fun deleteExtra(
        userId: UserId,
        key: String
    ): Result<Unit> {
        initialMap[userId]?.remove(key)
        return Result.success(Unit)
    }
}
