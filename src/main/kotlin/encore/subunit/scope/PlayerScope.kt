package encore.subunit.scope

import encore.datastore.collection.UserId

/**
 * A user-scoped context.
 *
 * Subunits using this scope operate on a single user's domain.
 * They may use [userId] to retrieve and persist user-specific data.
 *
 * @property userId User ID associated with this scope.
 */
data class UserScope(val userId: UserId) : SubunitScope
