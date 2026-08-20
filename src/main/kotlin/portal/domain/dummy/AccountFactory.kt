package portal.domain.dummy

import encore.time.TimeCenter
import encore.utils.hash
import encore.utils.identifier.Ids
import portal.domain.Members
import portal.mongo.collection.UserAccount

/**
 * Utilities to create dummy accounts.
 */
object AccountFactory {
    fun account(
        username: String = username(),
        displayName: String = displayName(),
        email: String = email(),
        password: String = "dummy",
        extra: Map<String, String> = emptyMap()
    ): UserAccount {
        val now = TimeCenter.now()
        return UserAccount(
            userId = Ids.uuid(),
            username = username,
            displayName = displayName,
            email = email,
            hashedPassword = hash(password),
            registeredAt = now,
            lastActiveAt = now,
            extra = extra
        )
    }

    /**
     * Produce string like "tomatosmart_431"
     */
    fun username(): String {
        val firstWord = Words.noun()
        val secondWord = Words.adjective()
        val thirdWord = Ids.random(3)
        return "$firstWord${secondWord}_$thirdWord"
    }

    /**
     * Produce string like "BurgerPerfect_Xiaoting"
     */
    fun displayName(): String {
        val firstWord = Words.capitalNoun()
        val secondWord = Words.capitalAdjective()
        val thirdWord = Members.all.random()
        return "$firstWord${secondWord}_$thirdWord"
    }

    /**
     * Produce string like "tomatosmart@email.com"
     */
    fun email(): String {
        val firstWord = Words.noun()
        val secondWord = Words.adjective()
        return "$firstWord$secondWord-${Ids.random(6)}@email.com"
    }
}
