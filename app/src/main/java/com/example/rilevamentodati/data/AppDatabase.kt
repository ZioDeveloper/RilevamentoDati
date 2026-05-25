package com.example.rilevamentodati.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Perizia::class, FotoPerizia::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun periziaDao(): PeriziaDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun create(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rilevamento_dati.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE perizie ADD COLUMN fotoPath TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS foto_perizie (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        periziaId INTEGER NOT NULL,
                        path TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(periziaId) REFERENCES perizie(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_foto_perizie_periziaId ON foto_perizie(periziaId)")
                db.execSQL(
                    """
                    INSERT INTO foto_perizie (periziaId, path, createdAt)
                    SELECT id, fotoPath, dataPerizia
                    FROM perizie
                    WHERE fotoPath IS NOT NULL
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE perizie ADD COLUMN idTelaioOrigine INTEGER")
                db.execSQL("ALTER TABLE perizie ADD COLUMN idCommessa INTEGER")
                db.execSQL("ALTER TABLE perizie ADD COLUMN isDekra INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_perizie_idTelaioOrigine ON perizie(idTelaioOrigine)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_perizie_idCommessa ON perizie(idCommessa)")
                db.execSQL("ALTER TABLE foto_perizie ADD COLUMN tipoDocumentoId INTEGER")
                db.execSQL("ALTER TABLE foto_perizie ADD COLUMN tipoDocumentoDescrizione TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_foto_perizie_tipoDocumentoId ON foto_perizie(tipoDocumentoId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM foto_perizie")
                db.execSQL("DELETE FROM perizie")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM foto_perizie")
                db.execSQL("DELETE FROM perizie")
            }
        }
    }
}
