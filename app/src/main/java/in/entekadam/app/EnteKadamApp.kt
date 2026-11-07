package in.entekadam.app

import android.app.Application
import androidx.room.Room
import in.entekadam.app.data.AppDatabase

class EnteKadamApp : Application() {
    val database by lazy { AppDatabase.get(this) }
}
