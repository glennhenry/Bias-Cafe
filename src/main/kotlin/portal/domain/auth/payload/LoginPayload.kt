package portal.domain.auth.payload

import kotlinx.serialization.Serializable

/**
 * The payload sent by client from `login.html` for the `/api/login` route.
 *
 * @property username
 * @property password
 */
@Serializable
data class LoginPayload(
    val username: String,
    val password: String
)
