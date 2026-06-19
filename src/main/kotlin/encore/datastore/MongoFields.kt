package encore.datastore

import com.mongodb.client.model.Filters
import encore.account.model.Profile
import encore.datastore.collection.UserAccount
import encore.datastore.collection.ServerObjects
import encore.datastore.collection.ServerObjectsId
import org.bson.conversions.Bson

// This file contains constants of Kotlin's data class fields' name
// that is used in Mongo queries.

/** `userId`*/
val FieldUserId = UserAccount::userId.name

/** `username`*/
val FieldUsername = UserAccount::username.name

/** `email`*/
val FieldEmail = UserAccount::email.name

/** `hashedPassword`*/
val FieldPassword = UserAccount::hashedPassword.name

/** `profile`*/
val FieldProfile = UserAccount::profile.name

/** `profile.lastActiveAt`*/
val FieldProfileLastActive = "$FieldProfile.${Profile::lastActiveAt.name}"

/** `dbId`*/
val ServerObjectsDbId = ServerObjects::dbId.name

/** Mongo filters for `dbId` equals [ServerObjectsId]*/
val ServerObjectsFilter: Bson = Filters.eq(ServerObjectsDbId, ServerObjectsId)
