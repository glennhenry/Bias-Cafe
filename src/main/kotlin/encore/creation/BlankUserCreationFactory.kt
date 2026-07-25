package encore.creation

import encore.utils.types.Report
import project.mongo.collection.UserAccount
import project.mongo.collection.UserId

class BlankUserCreationFactory: UserCreationFactory {
    override fun userId(isAdmin: Boolean): UserId {
        TODO("Not yet implemented")
    }

    override fun account(
        userId: UserId,
        username: String,
        password: String,
        email: String
    ): UserAccount {
        TODO("Not yet implemented")
    }


    override fun updateServerObjects(
        account: UserAccount
    ): Report {
        TODO("Not yet implemented")
    }
}
