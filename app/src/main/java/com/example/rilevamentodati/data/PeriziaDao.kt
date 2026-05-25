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

    @Query("SELECT path FROM foto_perizie WHERE periziaId IN (SELECT id FROM perizie WHERE isDekra = 0)")
    suspend fun getFotoPathsArchivioLocale(): List<String>

    @Query("DELETE FROM foto_perizie WHERE periziaId IN (SELECT id FROM perizie WHERE isDekra = 0)")
    suspend fun deleteFotoArchivioLocale()

    @Query("DELETE FROM perizie WHERE isDekra = 0")
    suspend fun deleteArchivioLocale()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(perizia: Perizia): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFoto(foto: List<FotoPerizia>)

    @Update
    suspend fun update(perizia: Perizia)

    @Query("DELETE FROM foto_perizie WHERE periziaId = :periziaId")
    suspend fun deleteFotoByPeriziaId(periziaId: Long)

    @Query("DELETE FROM foto_perizie WHERE id = :id")
    suspend fun deleteFotoById(id: Long)

    @Query("UPDATE perizie SET syncStatus = 'DA_INVIARE' WHERE id = :id")
    suspend fun markPeriziaDaInviare(id: Long)

    @Query("UPDATE perizie SET syncStatus = 'INVIATO' WHERE syncStatus = 'DA_INVIARE'")
    suspend fun markDaInviareAsInviato()

    @Query("DELETE FROM perizie WHERE id = :id")
    suspend fun deleteById(id: Long)
}

