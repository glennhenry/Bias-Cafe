package portal.routes.common

/**
 * The minimal model needed by any pages.
 *
 * This includes [AccountData] which is used to display the user's account information.
 */
data class BasicModel(
    val account: AccountData?
)
