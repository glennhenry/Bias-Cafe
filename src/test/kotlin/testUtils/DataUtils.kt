package testUtils

import encore.account.model.UserMetadata
import encore.datastore.collection.Profile
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.time.TimeCenter
import encore.utils.hash

fun createAccount(userId: UserId, username: String, password: String): UserAccount {
    val now = TimeCenter.now()
    return UserAccount(
        userId = userId,
        username = username,
        email = "$username@email.com",
        hashedPassword = hash(password),
        registeredAt = now,
        lastActiveAt = now,
        metadata = UserMetadata(),
    )
}

fun createProfile(userId: UserId): Profile {
    return Profile(userId)
}
