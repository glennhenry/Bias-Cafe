package testUtils

import encore.account.model.Profile
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.time.TimeCenter
import encore.utils.hash

fun createAccount(userId: UserId, username: String, password: String): UserAccount {
    return UserAccount(
        userId = userId,
        username = username,
        email = "$username@email.com",
        hashedPassword = hash(password),
        profile = createProfile(userId)
    )
}

fun createProfile(userId: UserId): Profile {
    val now = TimeCenter.now()
    return Profile(
        userId = userId,
        createdAt = now,
        lastActiveAt = now
    )
}
