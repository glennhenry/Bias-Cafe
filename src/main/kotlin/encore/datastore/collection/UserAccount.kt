package encore.datastore.collection

import encore.account.model.UserMetadata
import kotlinx.serialization.Serializable
import project.domain.profile.Profile

/**
 * Representation of a user's account.
 *
 * This model is used to represent a user account in the database.
 *
 * @property userId Unique identifier of the user.
 * @property username Unique identifier of the user, which is also used for login.
 * @property email Email address associated with this account.
 * @property hashedPassword Hashed version of the account's password.
 * @property registeredAt Epoch millis of the account's registration date.
 * @property lastActiveAt Epoch millis of the account's last activity.
 *                        This denotes the last time the user send a network
 *                        message to the server.
 * @property metadata Any extra or uncategorized information about the user.
 * @property profile Profile information of user.
 */
@Serializable
data class UserAccount(
    val userId: UserId,
    val username: String,
    val email: String,
    val hashedPassword: String,
    val registeredAt: Long,
    val lastActiveAt: Long,
    val metadata: UserMetadata,
    val profile: Profile
)

/**
 * Represents a unique user identifier.
 *
 * This alias is used so it's possible to centralize code modification
 * if the underlying type needs to be changed (e.g., to `Long` or `UUID`).
 */
typealias UserId = String
