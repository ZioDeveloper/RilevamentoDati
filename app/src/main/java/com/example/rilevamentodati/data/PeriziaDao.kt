package com.example.rilevamentodati.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriziaDao {
    @Query("SELECT * FROM perizie ORDER BY dataPerizia DESC")
    fun observeAll(): Flow<List<Perizia>>

    @Transaction
    @Query("SELECT * FROM perizie ORDER BY dataPerizia DESC")
    fun observeAllWithFoto(): Flow<List<PeriziaConFoto>>

    @Query("SELECT COUNT(*) FROM perizie WHERE syncStatus = 'DA_INVIARE'")
    fun observeDaInviareCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM perizie WHERE idTelaioOrigine = :idTelaioOrigine")
    suspend fun countByTelaioOrigine(idTelaioOrigine: Int): Int

    @Query("SELECT * FROM utenti_cache WHERE id = :utenteId")
    suspend fun getUtenteCache(utenteId: String): UtenteCache?

    @Query("SELECT * FROM commesse_cache WHERE utenteId = :utenteId ORDER BY descrizione")
    suspend fun getCommesseCacheByUtente(utenteId: String): List<CommessaCache>

    @Query("SELECT * FROM telai_cache WHERE idCommessa = :commessaId ORDER BY targa, telaio")
    suspend fun getTelaiCacheByCommessa(commessaId: Int): List<TelaioCache>

    @Query("SELECT * FROM telai_cache ORDER BY idCommessa, targa, telaio")
    fun observeAllTelaiCache(): Flow<List<TelaioCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUtenteCache(utente: UtenteCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCommesseCache(commesse: List<CommessaCache>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTelaiCache(telai: List<TelaioCache>)

    @Query("DELETE FROM commesse_cache WHERE utenteId = :utenteId")
    suspend fun deleteCommesseCacheByUtente(utenteId: String): Int

    @Query("DELETE FROM telai_cache WHERE idCommessa = :commessaId")
    suspend fun deleteTelaiCacheByCommessa(commessaId: Int): Int

    @Query("DELETE FROM telai_cache")
    suspend fun deleteAllTelaiCache(): Int

    @Query("DELETE FROM telai_cache WHERE idCommessa IN (:commessaIds)")
    suspend fun deleteTelaiCacheByCommesse(commessaIds: List<Int>): Int

    @Query("DELETE FROM telai_cache WHERE idCommessa NOT IN (:commessaIds)")
    suspend fun deleteTelaiCacheNotInCommesse(commessaIds: List<Int>): Int

    @Transaction
    @Query("SELECT * FROM perizie WHERE syncStatus = 'DA_INVIARE' ORDER BY dataPerizia ASC")
    suspend fun getDaInviareWithFoto(): List<PeriziaConFoto>

    @Transaction
    @Query("SELECT * FROM perizie WHERE syncStatus = 'INVIATO' ORDER BY dataPerizia ASC")
    suspend fun getInviateWithFoto(): List<PeriziaConFoto>

    @Query("SELECT path FROM foto_perizie WHERE periziaId IN (SELECT id FROM perizie WHERE isDekra = 0)")
    suspend fun getFotoPathsArchivioLocale(): List<String>

    @Query("SELECT path FROM foto_perizie WHERE periziaId = :periziaId")
    suspend fun getFotoPathsByPeriziaId(periziaId: Long): List<String>

    @Query("DELETE FROM foto_perizie WHERE periziaId IN (SELECT id FROM perizie WHERE isDekra = 0)")
    suspend fun deleteFotoArchivioLocale(): Int

    @Query("DELETE FROM perizie WHERE isDekra = 0")
    suspend fun deleteArchivioLocale(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(perizia: Perizia): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFoto(foto: List<FotoPerizia>)

    @Update
    suspend fun update(perizia: Perizia)

    @Query("DELETE FROM foto_perizie WHERE periziaId = :periziaId")
    suspend fun deleteFotoByPeriziaId(periziaId: Long): Int

    @Query("DELETE FROM foto_perizie WHERE id = :id")
    suspend fun deleteFotoById(id: Long): Int

    @Query("DELETE FROM foto_perizie WHERE periziaId IN (SELECT id FROM perizie WHERE syncStatus = 'INVIATO')")
    suspend fun deleteFotoInviate(): Int

    @Query("DELETE FROM perizie WHERE syncStatus = 'INVIATO'")
    suspend fun deletePerizieInviate(): Int

    @Query("UPDATE perizie SET syncStatus = 'DA_INVIARE' WHERE id = :id")
    suspend fun markPeriziaDaInviare(id: Long): Int

    @Query("UPDATE perizie SET syncStatus = 'INVIATO' WHERE id IN (:ids)")
    suspend fun markInviate(ids: List<Long>): Int

    @Query("UPDATE perizie SET syncStatus = 'DA_INVIARE' WHERE isDekra = 1 AND idCommessa = :commessaId AND syncStatus = 'INVIATO'")
    suspend fun markCommessaDaReinviare(commessaId: Int): Int

    @Query("DELETE FROM perizie WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
