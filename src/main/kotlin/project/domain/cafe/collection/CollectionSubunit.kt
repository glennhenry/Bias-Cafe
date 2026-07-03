package project.domain.cafe.collection

import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import project.routes.SectionItem
import project.routes.SpaceItem

/**
 * Server subunits that handles [Space] and [Section] concerns from [CollectionRepository].
 *
 * This is also the main component for cafe operation related to them
 * as they are rarely updated and can't be updated as of now.
 *
 * @property collectionRepository [CollectionRepository] implementation.
 */
class CollectionSubunit(
    private val collectionRepository: CollectionRepository
) : Subunit<ServerScope> {
    private lateinit var spaces: List<Space>
    private lateinit var sections: List<Section>

    fun getSpaces() = spaces
    fun getSections() = sections

    fun getSpacesForLandingModel(): List<SpaceItem> {
        val m = mutableMapOf<String, MutableList<Section>>()
        for (s in sections) {
            val x = m.getOrDefault(s.spaceId, mutableListOf()).also { it.add(s) }
            m[s.spaceId] = x
        }

        val y = mutableListOf<SpaceItem>()
        for ((spaceId, sections) in m) {
            val sp = requireNotNull(spaces.find { it.id == spaceId })
            val ex = y.find { it.name == sp.name }
            if (ex == null) {
                y.add(SpaceItem(sp.name, sections.sortedBy { it.order }.map { SectionItem(it.id, it.name) }))
            }
        }

        return y.sortedBy { x -> spaces.find { it.name == x.name }?.order }
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> {
        return runCatching {
            spaces = collectionRepository.getSpaces().getOrThrow()
            sections = collectionRepository.getSections().getOrThrow()
        }
    }

    override suspend fun disband(scope: ServerScope): Result<Unit> {
        return runCatching { }
    }

    companion object {
        /**
         * Creates a test instance of [CollectionSubunit].
         *
         * @param collectionRepository use [BlankCollectionRepository] when not under test.
         */
        fun createForTest(
            collectionRepository: CollectionRepository = BlankCollectionRepository()
        ): CollectionSubunit {
            return CollectionSubunit(collectionRepository)
        }
    }
}
