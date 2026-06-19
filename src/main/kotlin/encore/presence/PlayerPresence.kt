package encore.presence

import encore.datastore.collection.UserId

/**
 * Represents the current presence status of a user.
 *
 * @property userId Unique identifier of the user.
 * @property onlineSince The timestamp (in milliseconds since epoch)
 *                       when the user came online.
 * @property lastNetworkActivity The timestamp (in milliseconds since epoch)
 *                               of the user's most recent network activity.
 */
data class UserPresence(
    val userId: UserId,
    val onlineSince: Long,
    @Volatile
    var lastNetworkActivity: Long
)
