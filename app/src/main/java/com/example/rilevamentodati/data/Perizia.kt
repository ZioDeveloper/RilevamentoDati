package com.example.rilevamentodati.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter

@Entity(
    tableName = "perizie",
    indices = [Index("idTelaioOrigine"), Index("idCommessa")]
)
data class Perizia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dataPerizia: Long,
    val targa: String,
    val telaio: String,
    val modello: String,
    val fotoPath: String? = null,
    val syncStatus: SyncStatus = SyncStatus.DA_INVIARE,
    val idTelaioOrigine: Int? = null,
    val idCommessa: Int? = null,
    val isDekra: Boolean = false
)

@Entity(
    tableName = "foto_perizie",
    foreignKeys = [
        ForeignKey(
            entity = Perizia::class,
            parentColumns = ["id"],
            childColumns = ["periziaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("periziaId"), Index("tipoDocumentoId")]
)
data class FotoPerizia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periziaId: Long,
    val path: String,
    val createdAt: Long,
    val tipoDocumentoId: Int? = null,
    val tipoDocumentoDescrizione: String? = null
)

data class PeriziaConFoto(
    @androidx.room.Embedded
    val perizia: Perizia,
    @Relation(
        parentColumn = "id",
        entityColumn = "periziaId"
    )
    val foto: List<FotoPerizia>
)

enum class SyncStatus {
    DA_INVIARE,
    INVIATO,
    ERRORE
}

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}

@Entity(tableName = "utenti_cache")
data class UtenteCache(
    @PrimaryKey
    val id: String,
    val nome: String,
    val cognome: String,
    val passwordHash: String,
    val ultimoAggiornamento: Long,
    val ultimoLogin: Long? = null
)

@Entity(
    tableName = "commesse_cache",
    primaryKeys = ["utenteId", "id"],
    indices = [Index("utenteId"), Index("id")]
)
data class CommessaCache(
    val utenteId: String,
    val id: Int,
    val codice: String,
    val descrizione: String,
    val idCliente: Int?,
    val ultimoAggiornamento: Long
)

@Entity(
    tableName = "telai_cache",
    primaryKeys = ["idTelaio"],
    indices = [Index("idCommessa"), Index("targa"), Index("telaio")]
)
data class TelaioCache(
    val idTelaio: Int,
    val idCommessa: Int,
    val targa: String,
    val telaio: String?,
    val modello: String?,
    val dataIn: Long?,
    val idTecnico: Int?,
    val idGravita: Int?,
    val fila: String?,
    val annotazioni: String?,
    val fotoPresenti: Int,
    val fotoObbligatorie: Int,
    val sequenzaCompleta: Boolean,
    val ultimoAggiornamento: Long
)
