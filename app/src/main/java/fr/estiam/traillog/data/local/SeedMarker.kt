package fr.estiam.traillog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Records that the bundled initial data has been imported. */
@Entity(tableName = "seed_markers")
data class SeedMarker(
    @PrimaryKey val key: String
)

