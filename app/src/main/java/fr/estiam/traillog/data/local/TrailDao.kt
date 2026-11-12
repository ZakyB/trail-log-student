package fr.estiam.traillog.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails ORDER BY name COLLATE NOCASE, id")
    fun observeAll(): Flow<List<TrailEntity>>

    @Query("SELECT * FROM trails WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): TrailEntity?

    @Upsert
    suspend fun upsert(item: TrailEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<TrailEntity>)

    @Query("SELECT EXISTS(SELECT 1 FROM seed_markers WHERE `key` = :key)")
    suspend fun wasSeeded(key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun putSeedMarker(marker: SeedMarker)

    @Query("DELETE FROM trails WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM trails ORDER BY name COLLATE NOCASE, id")
    fun pageName(): PagingSource<Int, TrailEntity>

    @Query("SELECT * FROM trails ORDER BY distanceKm DESC, name COLLATE NOCASE, id")
    fun pageDistance(): PagingSource<Int, TrailEntity>

    @Query("SELECT * FROM trails WHERE isFavorite = 1 ORDER BY name COLLATE NOCASE, id")
    fun favoritesName(): PagingSource<Int, TrailEntity>

    @Query("SELECT * FROM trails WHERE isFavorite = 1 ORDER BY distanceKm DESC, name COLLATE NOCASE, id")
    fun favoritesDistance(): PagingSource<Int, TrailEntity>
}

