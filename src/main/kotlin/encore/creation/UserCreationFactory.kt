package encore.creation

import encore.utils.types.Report
import project.mongo.collection.UserAccount
import project.mongo.collection.UserId
import project.mongo.collection.ServerObjects

/**
 * Component responsible for managing database documents for new user.
 *
 * Implementation defines methods to produce new document to be
 * inserted to each respective collections and update existing
 * server data collection.
 */
interface UserCreationFactory {
    /**
     * Generate a new [UserId] for the user.
     * @param isAdmin whether the requested ID is intended for admin account.
     */
    fun userId(isAdmin: Boolean): UserId

    /**
     * Produce [UserAccount] for the user with the given
     * [userId], [username], [password], and [email].
     */
    fun account(
        userId: UserId, username: String,
        password: String, email: String
    ): UserAccount

    /**
     * Invoke side effects to the [ServerObjects] collection
     * for the new user.
     *
     * This may contain code like updating leaderboard, friends list, etc.
     *
     * @return [Report.Ok] if every operation succeeded, otherwise [Report.Fail].
     */
    fun updateServerObjects(account: UserAccount): Report
}
