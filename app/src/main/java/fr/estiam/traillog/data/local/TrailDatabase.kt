package fr.estiam.traillog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TrailEntity::class, SeedMarker::class], version = 2, exportSchema = false)
abstract class TrailDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao

    companion object {
        @Volatile
        private var instance: TrailDatabase? = null

        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `seed_markers` (" +
                        "`key` TEXT NOT NULL, PRIMARY KEY(`key`))"
                )
            }
        }

        fun get(context: Context): TrailDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                TrailDatabase::class.java,
                "traillog.db"
            ).addMigrations(migration1To2)
                .build()
                .also { instance = it }
        }
    }
}

