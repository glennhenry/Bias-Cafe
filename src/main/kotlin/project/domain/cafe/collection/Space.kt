package project.domain.cafe.collection

/**
 * Refer to the **space** concept in cafe.
 *
 * Group together multiple discussions with similar subject.
 *
 * @property id Unique string identifier of the space.
 *              Used for referential purpose.
 * @property name Display name of the space.
 * @property order The order of how distinct spaces are displayed in the cafe.
 */
data class Space(
    val id: String,
    val name: String,
    val order: Int
)
