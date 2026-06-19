package encore.acts

import encore.acts.choreo.BasicChoreography
import encore.datastore.collection.UserId

/**
 * Marker interface representing the input for a stage act.
 *
 * An `ActConcept` encapsulates all data required to execute a [StageAct].
 * This includes both static and runtime data, but excludes external dependencies.
 *
 * For instance, a building construction task for a user may include:
 * - [UserId] of the user
 * - `buildingId` identifiying the building being constructed.
 * - `finishAt` defining when the construction would finish, which is also used
 *   to determine the act's delay in [BasicChoreography].
 *
 * External dependencies (e.g., `BuildingSubunit`) should not be part of the concept
 * and must instead be injected into the [StageAct] implementation.
 *
 * All stage act input types should implement this interface.
 */
interface ActConcept
