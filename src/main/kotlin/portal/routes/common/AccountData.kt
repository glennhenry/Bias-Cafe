package portal.routes.common

/**
 * Data of user's account which is needed by most pages
 * to display account information.
 *
 * The account information itself is usually optional,
 * this typically happens when user is not logged in yet.
 *
 * @property username The username.
 * @property level Example data.
 */
data class AccountData(
    val username: String,
    val level: Int
)
