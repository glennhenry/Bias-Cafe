package encore.presence

import encore.datastore.collection.UserId
import encore.fancam.Fancam
import encore.fancam.Tags
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.time.TimeCenter
import java.util.concurrent.ConcurrentHashMap

/**
 * Server subunit for tracking users presence status.
 *
 * This is used for:
 * - Tracking user's activity such as whether they are online or offline.
 * - Get the last active time of a user.
 *
 * Typically, user presence is determined from the network activity of the socket server.
 */
class UserPresenceSubunit : Subunit<ServerScope> {
    private val onlineUsers = ConcurrentHashMap<String, UserPresence>()

    /**
     * Mark the [userId] as online.
     *
     * Does nothing if:
     * - the user is already online
     */
    fun markOnline(userId: UserId) {
        val now = TimeCenter.now()
        onlineUsers[userId] = UserPresence(
            userId = userId,
            onlineSince = now,
            lastNetworkActivity = now,
        )
        Fancam.trace(Tags.Presence) { "UserId $userId is now online" }
    }

    /**
     * Mark the [userId] as offline. Does nothing if the user is already offline.
     */
    fun markOffline(userId: UserId) {
        onlineUsers.remove(userId)
        Fancam.trace(Tags.Presence) { "UserId $userId is now offline" }
    }

    /**
     * Returns whether user with [userId] is currently online.
     */
    fun isOnline(userId: UserId): Boolean {
        return onlineUsers.contains(userId)
    }

    /**
     * Returns whether user with [userId] is currently online.
     */
    fun isOffline(userId: UserId): Boolean {
        return !onlineUsers.contains(userId)
    }

    /**
     * Update the last network activity of [userId]. Does nothing if the user is not online.
     */
    fun updateLastActivity(userId: UserId) {
        onlineUsers[userId]?.lastNetworkActivity = TimeCenter.now()
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> = Result.success(Unit)
    override suspend fun disband(scope: ServerScope): Result<Unit> {
        onlineUsers.clear()
        return Result.success(Unit)
    }
}
