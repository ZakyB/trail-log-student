package fr.estiam.traillog.data.local

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.map
import androidx.room.withTransaction
import fr.estiam.traillog.data.TrailRepository
import fr.estiam.traillog.model.Difficulty
import fr.estiam.traillog.model.Trail
import fr.estiam.traillog.model.TrailSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

class RoomTrailRepository(
    context: Context,
    private val db: TrailDatabase,
    private val dao: TrailDao
) : TrailRepository {
    private val appContext = context.applicationContext

    override fun pagingSource(
        sort: TrailSortOrder,
        favoritesOnly: Boolean
    ): PagingSource<Int, Trail> {
        val source = when {
            favoritesOnly && sort == TrailSortOrder.DISTANCE -> dao.favoritesDistance()
            favoritesOnly -> dao.favoritesName()
            sort == TrailSortOrder.DISTANCE -> dao.pageDistance()
            else -> dao.pageName()
        }
        return source.map(TrailEntity::toDomain)
    }

    override fun observeTrails(): Flow<List<Trail>> =
        dao.observeAll().map { rows -> rows.map(TrailEntity::toDomain) }

    override suspend fun getTrail(id: String): Trail? =
        dao.findById(id)?.toDomain()

    override suspend fun save(trail: Trail) {
        require(trail.isValid()) { "Invalid trail: id and name must be nonblank, and distance/elevation must be nonnegative." }
        dao.upsert(trail.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override suspend fun seedIfNeeded() {
        val markerKey = "initial_trails"
        if (dao.wasSeeded(markerKey)) return

        val initialTrails = readAndValidateAssets()
        db.withTransaction {
            if (!dao.wasSeeded(markerKey)) {
                dao.insertAll(initialTrails.map(Trail::toEntity))
                dao.putSeedMarker(SeedMarker(markerKey))
            }
        }
    }

    private fun readAndValidateAssets(): List<Trail> {
        val json = appContext.assets.open("trails.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val array = JSONArray(json)
        val seenIds = mutableSetOf<String>()

        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            val id = obj.getString("id").trim()
            val name = obj.getString("name").trim()
            val difficultyName = obj.getString("difficulty")
            val difficulty = Difficulty.entries.firstOrNull { it.name == difficultyName }
                ?: error("Unknown difficulty '$difficultyName' at JSON index $index")
            val trail = Trail(
                id = id,
                name = name,
                region = obj.getString("region"),
                distanceKm = obj.getDouble("distanceKm"),
                elevationGainM = obj.getInt("elevationGainM"),
                difficulty = difficulty,
                notes = obj.optString("notes", ""),
                isFavorite = obj.optBoolean("isFavorite", false),
                updatedAt = 0L
            )

            require(trail.isValid()) { "Invalid trail at JSON index $index: $id" }
            require(seenIds.add(id)) { "Duplicate trail ID in JSON: $id" }
            trail
        }
    }
}

