package portal.domain.auth.payload

import kotlinx.serialization.Serializable

/**
 * The payload sent by client from `register.html` for the `/api/register` route.
 *
 * @property username
 * @property email
 * @property password
 */
@Serializable
data class RegisterPayload(
    val username: String,
    val email: String,
    val password: String
)
