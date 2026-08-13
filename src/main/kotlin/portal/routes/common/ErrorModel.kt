package portal.routes.common

/**
 * Model for error page (`error.html`).
 *
 * The error page shows a notice that the requested page
 * or resource is unavailable to the user.
 *
 * @property account Optional [AccountData] of user.
 * @property title Title of the webpage.
 * @property heading The heading text — describe what happened shortly
 *                   (e.g., "Require Login", "Not Found", "Maintenance Ongoing").
 * @property message The detailed messaage on what happpened.
 * @property action Optional HTML action to link to a particular page.
 *                  This is typically used as a fallback (e.g., a page that needs
 *                  user to be logged in may use this to add a link to the login page).
 */
data class ErrorModel(
    val account: AccountData?,
    val title: String,
    val heading: String,
    val message: String,
    val action: Action? = null
)

/**
 * Data needed to represent HTML's <a> tag.
 *
 * @property href The `<a href>` to be used.
 * @property text The text displaying the link.
 */
data class Action(
    val href: String,
    val text: String
)
