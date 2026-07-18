package testUtils

import encore.account.model.UserMetadata
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.time.TimeCenter
import encore.utils.hash
import project.domain.profile.Profile

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
        profile = createProfile()
    )
}

fun createProfile(): Profile {
    return Profile(
        displayName = "",
        avatarUrl = "",
        level = 1
    )
}
