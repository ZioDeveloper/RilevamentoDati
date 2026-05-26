package com.example.rilevamentodati.data

import android.content.Context
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class PuliziaFotoResult(
    val fotoCancellate: Int,
    val perizieCancellate: Int
)

data class TrasferimentoResult(
    val file: File,
    val perizie: Int,
    val foto: Int,
    val periziaIds: List<Long> = emptyList()
)

data class InvioApiResult(
    val file: File,
    val perizie: Int,
    val foto: Int,
    val response: String
)

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

    suspend fun segnaCommessaDaReinviare(commessaId: Int) {
        dao.markCommessaDaReinviare(commessaId)
    }

    suspend fun pulisciFotoInviate(): PuliziaFotoResult {
        val perizieInviate = dao.getInviateWithFoto()
        val fotoPaths = perizieInviate.flatMap { perizia ->
            perizia.foto.map { it.path }
        }
        var cancellate = 0
        fotoPaths.forEach { path ->
            val file = File(path)
            if (file.exists() && file.delete()) {
                cancellate++
            }
        }
        dao.deleteFotoInviate()
        dao.deletePerizieInviate()
        return PuliziaFotoResult(
            fotoCancellate = cancellate,
            perizieCancellate = perizieInviate.size
        )
    }

    suspend fun creaPacchettoTrasferimento(
        context: Context,
        marcaInviate: Boolean = true
    ): TrasferimentoResult? {
        val perizieDaInviare = dao.getDaInviareWithFoto()
        if (perizieDaInviare.isEmpty()) return null

        val exportDir = File(context.filesDir, "trasferimenti").apply { mkdirs() }
        val exportFile = File(exportDir, "dekra_${System.currentTimeMillis()}.zip")
        val manifest = creaManifestTrasferimento(perizieDaInviare)
        var fotoInserite = 0

        ZipOutputStream(exportFile.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            perizieDaInviare.forEach { periziaConFoto ->
                val targa = nomeFileSicuro(periziaConFoto.perizia.targa)
                periziaConFoto.foto.forEach { foto ->
                    val fotoFile = File(foto.path)
                    if (fotoFile.exists()) {
                        val tipo = foto.tipoDocumentoId?.toString() ?: "senza_tipo"
                        val nomeFoto = "foto/$targa/${foto.id}_${tipo}_${nomeFileSicuro(fotoFile.name)}"
                        zip.putNextEntry(ZipEntry(nomeFoto))
                        fotoFile.inputStream().use { input -> input.copyTo(zip) }
                        zip.closeEntry()
                        fotoInserite++
                    }
                }
            }
        }

        val periziaIds = perizieDaInviare.map { it.perizia.id }
        if (marcaInviate) {
            dao.markInviate(periziaIds)
        }

        return TrasferimentoResult(
            file = exportFile,
            perizie = perizieDaInviare.size,
            foto = fotoInserite,
            periziaIds = periziaIds
        )
    }

    suspend fun inviaPacchettoApi(context: Context, endpoint: String): InvioApiResult? {
        val pacchetto = creaPacchettoTrasferimento(
            context = context,
            marcaInviate = false
        ) ?: return null

        val response = uploadZip(endpoint, pacchetto.file)
        dao.markInviate(pacchetto.periziaIds)
        return InvioApiResult(
            file = pacchetto.file,
            perizie = pacchetto.perizie,
            foto = pacchetto.foto,
            response = response
        )
    }

    suspend fun elimina(perizia: Perizia) {
        dao.deleteById(perizia.id)
    }

    private fun creaManifestTrasferimento(perizie: List<PeriziaConFoto>): JSONObject {
        val root = JSONObject()
        root.put("formato", "RilevamentoDati-DEKRA-1")
        root.put("creatoIl", System.currentTimeMillis())
        root.put("perizie", JSONArray().apply {
            perizie.forEach { item ->
                val perizia = item.perizia
                put(JSONObject().apply {
                    put("idLocale", perizia.id)
                    put("dataPerizia", perizia.dataPerizia)
                    put("targa", perizia.targa)
                    put("telaio", perizia.telaio)
                    put("modello", perizia.modello)
                    put("idCommessa", perizia.idCommessa)
                    put("idTelaioOrigine", perizia.idTelaioOrigine)
                    put("isDekra", perizia.isDekra)
                    put("foto", JSONArray().apply {
                        item.foto.forEach { foto ->
                            put(JSONObject().apply {
                                put("idLocale", foto.id)
                                put("periziaIdLocale", foto.periziaId)
                                put("tipoDocumentoId", foto.tipoDocumentoId)
                                put("tipoDocumentoDescrizione", foto.tipoDocumentoDescrizione)
                                put("createdAt", foto.createdAt)
                                put("nomeFile", File(foto.path).name)
                                put("filePresente", File(foto.path).exists())
                            })
                        }
                    })
                })
            }
        })
        return root
    }

    private fun nomeFileSicuro(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }

    private suspend fun uploadZip(endpoint: String, file: File): String = withContext(Dispatchers.IO) {
        val boundary = "RilevamentoDatiBoundary${System.currentTimeMillis()}"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 120_000
            doInput = true
            doOutput = true
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        try {
            DataOutputStream(connection.outputStream).use { output ->
                output.writeBytes("--$boundary\r\n")
                output.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"${nomeFileSicuro(file.name)}\"\r\n"
                )
                output.writeBytes("Content-Type: application/zip\r\n\r\n")
                file.inputStream().use { input -> input.copyTo(output) }
                output.writeBytes("\r\n--$boundary--\r\n")
                output.flush()
            }

            val code = connection.responseCode
            val responseStream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val response = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                error("API import non riuscita ($code): ${response.ifBlank { connection.responseMessage }}")
            }

            response
        } finally {
            connection.disconnect()
        }
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
