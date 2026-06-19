package encore.account.model

import encore.datastore.collection.UserId
import kotlinx.serialization.Serializable

/**
 * Representation of an account credentials in database.
 *
 * @property userId Unique identifier of the account.
 * @property hashedPassword hashed representation of the account's password.
 */
@Serializable
data class Credentials(
    val userId: UserId,
    val hashedPassword: String
)
