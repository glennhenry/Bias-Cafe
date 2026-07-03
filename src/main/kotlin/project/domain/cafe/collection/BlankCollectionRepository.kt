package project.domain.cafe.collection

class BlankCollectionRepository: CollectionRepository {
    override suspend fun getSpaces(): Result<List<Space>> = TODO("NO OPERATION")
    override suspend fun getSections(): Result<List<Section>> = TODO("NO OPERATION")
}
