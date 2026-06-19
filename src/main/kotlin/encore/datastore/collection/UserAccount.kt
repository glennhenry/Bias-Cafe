package encore.datastore.collection

import kotlinx.serialization.Serializable
import encore.account.model.Profile

/**
 * Representation of a user's account.
 *
 * This model is used to represent a user account in the database.
 *
 * @property userId Unique identifier of the user.
 * @property username Display name of the user.
 * @property email Email address associated with this account.
 * @property hashedPassword Hashed version of the account's password.
 * @property profile Representation of the user's profile.
 */
@Serializable
data class UserAccount(
    val userId: UserId,
    val username: String,
    val email: String,
    val hashedPassword: String,
    val profile: Profile,
)

/**
 * Represents a unique user identifier.
 *
 * This alias is used so it's possible to centralize code modification
 * if the underlying type needs to be changed (e.g., to `Long` or `UUID`).
 */
typealias UserId = String
