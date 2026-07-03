package project.domain.cafe.collection

/**
 * Repository for [Space] and [Section] data collection.
 *
 * Implementation solely provide way to access them.
 * Since space and section is rarely changed, administrator
 * may add collection directly to the underlying database editor.
 */
interface CollectionRepository {
    /**
     * Get every available spaces.
     */
    suspend fun getSpaces(): Result<List<Space>>

    /**
     * Get every available sections.
     */
    suspend fun getSections(): Result<List<Section>>
}
