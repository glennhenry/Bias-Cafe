package encore.account.model

import kotlinx.serialization.Serializable

/**
 * Additional information for the user.
 *
 * `UserMetadata` contains non-gameplay data managed at server-level.
 * It typically includes extra data like flags, ban, permissions, or
 * any other metadata for temporary or administrative purposes.
 */
@Serializable
data class UserMetadata(
    val flags: Map<String, Boolean> = emptyMap(),
    val extra: Map<String, String> = emptyMap(),
)
