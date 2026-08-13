package portal.domain.dummy

import encore.utils.identifier.Ids
import portal.domain.Members

/**
 * Utilities to create dummy accounts.
 */
object AccountFactory {
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
     * Produce string like "Cute Yujin"
     */
    fun displayName(): String {
        val firstWord = Words.capitalAdjective()
        val secondWord = Members.all.random()
        return "$firstWord $secondWord"
    }

    /**
     * Produce string like "tomatosmart@email.com"
     */
    fun email(): String {
        val firstWord = Words.noun()
        val secondWord = Words.adjective()
        return "$firstWord$secondWord@email.com"
    }
}
