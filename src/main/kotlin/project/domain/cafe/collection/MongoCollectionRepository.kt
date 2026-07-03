package project.domain.cafe.collection

import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.runMongoCatching
import kotlinx.coroutines.flow.toList

/** `order` */
val FieldSpaceOrder = Space::order.name

/** `order` */
val FieldSectionOrder = Section::order.name

class MongoCollectionRepository(
    private val spaceCollection: MongoCollection<Space>,
    private val sectionCollection: MongoCollection<Section>,
) : CollectionRepository {
    override suspend fun getSpaces(): Result<List<Space>> {
        return runMongoCatching {
            spaceCollection
                .find()
                .sort(Sorts.descending(FieldSpaceOrder))
                .toList()
        }
    }

    override suspend fun getSections(): Result<List<Section>> {
        return runMongoCatching {
            sectionCollection
                .find()
                .sort(Sorts.descending(FieldSectionOrder))
                .toList()
        }
    }
}
