package com.example.rilevamentodati.data

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class DekraCommessaSeed(
    val id: Int,
    val codice: String,
    val descrizione: String,
    val idCliente: Int = 35
)

data class DekraTipoDocumentoSeed(
    val id: Int,
    val descrizione: String,
    val ordinePerizia: Int,
    val numMinFoto: Int
)

data class DekraTelaioSeed(
    val id: Int,
    val telaio: String,
    val modello: String,
    val targa: String,
    val idCommessa: Int,
    val dataIn: String,
    val idTecnico: Int?,
    val idGravita: Int?,
    val fila: String? = null,
    val annotazioni: String? = null
)

object DekraSeedData {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    val commesse = listOf(
        DekraCommessaSeed(81, "DEKSTLFMC", "DEKRA STELLANTIS FIUMICINO"),
        DekraCommessaSeed(82, "DKR RIV", "DEKRA STELLANTIS RIVALTA"),
        DekraCommessaSeed(83, "DKR CSO", "DEKRA STELLANTIS CASSINO"),
        DekraCommessaSeed(84, "DKRBERCSO", "DEKRA BERTANI CASSINO"),
        DekraCommessaSeed(88, "DKR CST", "DEKRA BERTANI CASTIGLIONE"),
        DekraCommessaSeed(89, "DKR POM", "DEKRA POMIGLIANO"),
        DekraCommessaSeed(94, "test", "test")
    )

    val tipiDocumentoGuidati = listOf(
        DekraTipoDocumentoSeed(8, "3/4 ant sx (con targa visibile)", 1, 1),
        DekraTipoDocumentoSeed(24, "Numero telaio", 2, 1),
        DekraTipoDocumentoSeed(9, "Interni ant sx", 3, 1),
        DekraTipoDocumentoSeed(10, "Cruscotto con motore in funzione", 4, 1),
        DekraTipoDocumentoSeed(11, "Km da vicino", 5, 1),
        DekraTipoDocumentoSeed(12, "Navigatore in funzione/infotainment", 6, 1),
        DekraTipoDocumentoSeed(13, "Chiavi con key code e codice radio", 7, 1),
        DekraTipoDocumentoSeed(14, "Interni post sx", 8, 1),
        DekraTipoDocumentoSeed(15, "3/4 post sx (con targa visibile)", 9, 1),
        DekraTipoDocumentoSeed(16, "Vano baule con tendalino/cappelliera", 10, 1),
        DekraTipoDocumentoSeed(17, "Ruota di scorta o eventuale kit", 11, 1),
        DekraTipoDocumentoSeed(18, "3/4 post dx (con targa visibile)", 12, 1),
        DekraTipoDocumentoSeed(19, "Ruota post dx", 13, 1),
        DekraTipoDocumentoSeed(20, "Dettagli pneumatico 1", 14, 1),
        DekraTipoDocumentoSeed(21, "Spessore pneumatico", 15, 1),
        DekraTipoDocumentoSeed(22, "3/4 ant dx (con targa visibile)", 16, 1),
        DekraTipoDocumentoSeed(25, "Eventuali danni", 17, 1)
    )

    val tipiDocumentoExtra = tipiDocumentoGuidati +
        DekraTipoDocumentoSeed(23, "Dettagli pneumatico 2", 999, 1)

    fun tipoDocumento(id: Int?): DekraTipoDocumentoSeed? {
        return tipiDocumentoExtra.firstOrNull { it.id == id }
    }

    fun tipoDocumentoDanni(): DekraTipoDocumentoSeed {
        return tipoDocumento(25) ?: tipiDocumentoGuidati.last()
    }

    val telai = listOf(
        DekraTelaioSeed(17102, "VYFCRZYA9SZA13933", "FIAT GRANDE PANDA ELETTRICA LA PRIMA", "HA808FV", 88, "2026-04-07 15:09:11.083", null, 1),
        DekraTelaioSeed(17103, "VYCUHZKW0R4154418", "LANCIA NUOVA YPSILON ELETTRICA MY24 LX 1", "HA893PJ", 88, "2026-04-07 15:09:11.107", null, 1),
        DekraTelaioSeed(17104, "VYCUHZKW8R4114961", "LANCIA NUOVA YPSILON ELETTRICA MY24 LX 1", "HA897PJ", 88, "2026-04-07 15:09:11.110", null, 1),
        DekraTelaioSeed(17105, "VYCUHZKW8R4169300", "LANCIA NUOVA YPSILON ELETTRICA MY24 LX 1", "HA898PJ", 88, "2026-04-07 15:09:11.110", null, 1),
        DekraTelaioSeed(17106, "ZAA5AWAT2RJA40369", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA918PJ", 88, "2026-04-07 15:09:11.113", null, 1),
        DekraTelaioSeed(17107, "ZAA5AWAT5RJA40379", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA958PJ", 88, "2026-04-07 15:09:11.117", null, 1),
        DekraTelaioSeed(17108, "ZAA5AWAT6RJA40486", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA967PJ", 88, "2026-04-07 15:09:11.120", null, 1),
        DekraTelaioSeed(17109, "ZARPAHPX6R7E04463", "STELVIO DIESEL 210 CV VELOCE", "HA967ZZ", 88, "2026-04-07 15:09:11.123", null, 1),
        DekraTelaioSeed(17110, "ZAA5AWAT7RJA39833", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA971PJ", 88, "2026-04-07 15:09:11.127", null, 1),
        DekraTelaioSeed(17111, "ZAA5AWAT7RJA40707", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA973PJ", 88, "2026-04-07 15:09:11.130", null, 1),
        DekraTelaioSeed(17112, "ZAA5AWAT8RJA40490", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA979PJ", 88, "2026-04-07 15:09:11.130", null, 1),
        DekraTelaioSeed(17113, "ZAA5AWAT8RJA40375", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA981PJ", 88, "2026-04-07 15:09:11.133", null, 1),
        DekraTelaioSeed(17114, "ZAA5AWAT9RJA39784", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA984PJ", 88, "2026-04-07 15:09:11.137", null, 1),
        DekraTelaioSeed(17115, "ZAA5AWATXRJA40264", "ALFA JUNIOR IBRIDA MY25 Q4 INTENSA 1.2 1", "HA988PJ", 88, "2026-04-07 15:09:11.140", null, 1),
        DekraTelaioSeed(17116, "ZARPAHPX8R7E04464", "STELVIO DIESEL 210 CV VELOCE", "HA128ZZ", 88, "2026-04-07 15:09:11.140", null, 1),
        DekraTelaioSeed(17117, "VYCUHZKW3R4200467", "LANCIA NUOVA YPSILON ELETTRICA MY24 LX 1", "HA136FW", 88, "2026-04-07 15:09:11.143", null, 1),
        DekraTelaioSeed(17118, "ZARPAHPX5R7E04468", "STELVIO DIESEL 210 CV VELOCE", "HA137ZZ", 88, "2026-04-07 15:09:11.147", null, 1),
        DekraTelaioSeed(17119, "VYFCRZYA3SZA13359", "FIAT GRANDE PANDA ELETTRICA LA PRIMA", "HA156FW", 88, "2026-04-07 15:09:11.147", null, 1),
        DekraTelaioSeed(17120, "VYFCRZYA4SZA13564", "FIAT GRANDE PANDA ELETTRICA LA PRIMA", "HA163FW", 88, "2026-04-07 15:09:11.150", null, 1),
        DekraTelaioSeed(17121, "VYFCRZYA8SZA13406", "FIAT GRANDE PANDA ELETTRICA LA PRIMA", "HA198FW", 88, "2026-04-07 15:09:11.150", null, 1)
    )

    private val utenteCommesse = mapOf(
        "C001" to setOf(81, 82, 83, 84, 88, 89, 94),
        "VLR" to setOf(81, 82, 83, 84, 88),
        "DEL" to setOf(81, 82, 88),
        "GES" to setOf(83, 84, 89),
        "VZS" to setOf(81),
        "ADP" to setOf(81),
        "ALU" to setOf(81),
        "IOV" to setOf(81),
        "FIE" to setOf(82),
        "ZAN" to setOf(82),
        "DMA" to setOf(82),
        "CAPM" to setOf(82),
        "TTT" to setOf(88),
        "CHS" to setOf(88)
    )

    fun commessePerUtente(utenteId: String): List<DekraCommessaSeed> {
        val ids = utenteCommesse[utenteId.trim().uppercase()].orEmpty()
        return commesse.filter { it.id in ids }
    }

    fun commesseCachePerUtente(utenteId: String, aggiornamento: Long): List<CommessaCache> {
        val codiceUtente = utenteId.trim().uppercase()
        return commessePerUtente(codiceUtente).map { commessa ->
            CommessaCache(
                utenteId = codiceUtente,
                id = commessa.id,
                codice = commessa.codice,
                descrizione = commessa.descrizione,
                idCliente = commessa.idCliente,
                ultimoAggiornamento = aggiornamento
            )
        }
    }

    fun telaiCache(aggiornamento: Long): List<TelaioCache> {
        val fotoObbligatorie = tipiDocumentoGuidati.sumOf { it.numMinFoto }
        return telai.map { telaio ->
            TelaioCache(
                idTelaio = telaio.id,
                idCommessa = telaio.idCommessa,
                targa = telaio.targa,
                telaio = telaio.telaio,
                modello = telaio.modello,
                dataIn = dataInMillis(telaio.dataIn),
                idTecnico = telaio.idTecnico,
                idGravita = telaio.idGravita,
                fila = telaio.fila,
                annotazioni = telaio.annotazioni,
                fotoPresenti = 0,
                fotoObbligatorie = fotoObbligatorie,
                sequenzaCompleta = false,
                ultimoAggiornamento = aggiornamento
            )
        }
    }

    fun commessa(id: Int?): DekraCommessaSeed? {
        return commesse.firstOrNull { it.id == id }
    }

    fun dataInMillis(value: String): Long {
        return LocalDateTime.parse(value, dateFormatter)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
