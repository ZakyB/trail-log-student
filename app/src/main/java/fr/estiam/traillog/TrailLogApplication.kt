package fr.estiam.traillog

import android.app.Application
import fr.estiam.traillog.data.*
import fr.estiam.traillog.data.local.RoomTrailRepository
import fr.estiam.traillog.data.local.TrailDatabase
import fr.estiam.traillog.worker.TrailReminderWorker

/** STARTER M3: replace memory implementations during Lab 1 and Lab 3. */
class TrailLogApplication : Application() {
    private val database: TrailDatabase by lazy { TrailDatabase.get(this) }
    val trails: TrailRepository by lazy {
        RoomTrailRepository(this, database, database.trailDao())
    }
    val settings: PreferencesRepository by lazy { MemoryPreferencesRepository() } // TODO M3-LAB3: DataStore
    override fun onCreate() { super.onCreate(); TrailReminderWorker.ensureChannel(this) }
}

