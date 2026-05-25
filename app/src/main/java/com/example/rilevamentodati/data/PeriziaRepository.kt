package com.example.rilevamentodati.data

import kotlinx.coroutines.flow.Flow

class PeriziaRepository(
    private val dao: PeriziaDao
) {
    val perizie: Flow<List<PeriziaConFoto>> = dao.observeAllWithFoto()
    val daInviareCount: Flow<Int> = dao.observeDaInviareCount()

    suspend fun prepareDekraOffline() {
        // Le commesse DEKRA restano nei dati statici; le perizie nascono solo da "Nuova targa".
    }

    suspend fun salva(targa: String, telaio: String, modello: String, fotoPaths: List<String>) {
        val periziaId = dao.insert(
            Perizia(
                dataPerizia = System.currentTimeMillis(),
                targa = targa.trim().uppercase(),
                telaio = telaio.trim().uppercase(),
                modello = modello.trim()
            )
        )
        inserisciFoto(periziaId, fotoPaths)
    }

    suspend fun creaDekraPerizia(commessaId: Int, targa: String) {
        dao.insert(
            Perizia(
                dataPerizia = System.currentTimeMillis(),
                targa = targa.trim().uppercase(),
                telaio = "",
                modello = "",
                idCommessa = commessaId,
                isDekra = true,
                syncStatus = SyncStatus.DA_INVIARE
            )
        )
    }

    suspend fun aggiorna(
        perizia: Perizia,
        targa: String,
        telaio: String,
        modello: String,
        fotoPaths: List<String>
    ) {
        dao.update(
            perizia.copy(
                targa = targa.trim().uppercase(),
                telaio = telaio.trim().uppercase(),
                modello = modello.trim(),
                syncStatus = SyncStatus.DA_INVIARE
            )
        )
        dao.deleteFotoByPeriziaId(perizia.id)
        inserisciFoto(perizia.id, fotoPaths)
    }

    suspend fun aggiungiFotoGuidata(
        periziaId: Long,
        tipoDocumento: DekraTipoDocumentoSeed,
        path: String
    ) {
        dao.insertFoto(
            listOf(
                FotoPerizia(
                    periziaId = periziaId,
                    path = path,
                    createdAt = System.currentTimeMillis(),
                    tipoDocumentoId = tipoDocumento.id,
                    tipoDocumentoDescrizione = tipoDocumento.descrizione
                )
            )
        )
        dao.markPeriziaDaInviare(periziaId)
    }

    suspend fun rimuoviFotoGuidata(foto: FotoPerizia) {
        dao.deleteFotoById(foto.id)
        dao.markPeriziaDaInviare(foto.periziaId)
    }

    suspend fun inviaDati() {
        dao.markDaInviareAsInviato()
    }

    suspend fun elimina(perizia: Perizia) {
        dao.deleteById(perizia.id)
    }

    private suspend fun inserisciFoto(periziaId: Long, fotoPaths: List<String>) {
        if (fotoPaths.isEmpty()) return

        dao.insertFoto(
            fotoPaths.map { path ->
                FotoPerizia(
                    periziaId = periziaId,
                    path = path,
                    createdAt = System.currentTimeMillis()
                )
            }
        )
    }
}