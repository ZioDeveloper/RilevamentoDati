package com.example.rilevamentodati

import android.app.Application
import com.example.rilevamentodati.data.AppDatabase
import com.example.rilevamentodati.data.PeriziaRepository
import java.io.File

class RilevamentoDatiApp : Application() {
    val database by lazy { AppDatabase.create(this) }
    val repository by lazy { PeriziaRepository(database.periziaDao()) }

    override fun onCreate() {
        super.onCreate()
        pulisciFotoSviluppoUnaVolta()
    }

    private fun pulisciFotoSviluppoUnaVolta() {
        val prefs = getSharedPreferences("sviluppo", MODE_PRIVATE)
        if (prefs.getBoolean("pulizia_foto_v6", false)) return

        File(filesDir, "perizia_foto").deleteRecursively()
        prefs.edit().putBoolean("pulizia_foto_v6", true).apply()
    }
}