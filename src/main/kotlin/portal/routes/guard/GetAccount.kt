package portal.routes.guard

import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import portal.mongo.collection.UserAccount
import portal.routes.common.AccountData

/**
 * Ktor's [Attributes] key to get user's account produced from the session cookie
 */
val SessionAccountKey = AttributeKey<UserAccount>("account")

/**
 * Get [UserAccount] from the call's attributes.
 * This should only be called on `requireAccountGuard`.
 *
 * @throws IllegalArgumentException if not found.
 */
fun Attributes.getUserAccount(): UserAccount {
    return requireNotNull(getOrNull(SessionAccountKey)) {
        "Expected UserAccount but null"
    }
}

/**
 * Get [UserAccount] from the call's attributes.
 * @return `null` if not found.
 */
fun Attributes.getUserAccountOrNull(): UserAccount? {
    return getOrNull(SessionAccountKey)
}

/**
 * Get [AccountData]. This is used by most pages to fulfill
 * the account data of the view models.
 */
fun Attributes.getAccountData(): AccountData? {
    getOrNull(SessionAccountKey)?.let {
        return AccountData(
            username = it.username
        )
    }
    return null
}
