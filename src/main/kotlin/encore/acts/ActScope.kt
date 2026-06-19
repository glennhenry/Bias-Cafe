package encore.acts

import encore.datastore.collection.UserId
import encore.datastore.collection.ServerId
import kotlinx.coroutines.CoroutineScope

/**
 * Represent the scope to which a [StageAct] is bound to.
 *
 * A stage act is bound to an owner that determines its lifetime.
 * The owner can be a particular user or the server itself.
 *
 * For example, if an act is bound to a user and their connection is dead,
 * the act will seen as invalid, and thus cancelled. This is determined
 * through the owner's [coroutineScope].
 */
data class ActScope(
    /**
     * The globally unique identifier of the [StageAct] owner.
     *
     * This is used for debugging and diagnostic purpose.
     *
     * For user this should be [UserId].
     * Server-owned acts should use the [ServerId].
     */
    val ownerId: String,

    /**
     * The coroutine scope which the act will run on.
     */
    val coroutineScope: CoroutineScope
)
