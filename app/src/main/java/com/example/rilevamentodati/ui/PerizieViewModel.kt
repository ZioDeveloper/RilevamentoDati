package com.example.rilevamentodati.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rilevamentodati.data.CommessaCache
import com.example.rilevamentodati.data.DekraTipoDocumentoSeed
import com.example.rilevamentodati.data.FotoPerizia
import com.example.rilevamentodati.data.Perizia
import com.example.rilevamentodati.data.PeriziaConFoto
import com.example.rilevamentodati.data.PeriziaRepository
import com.example.rilevamentodati.data.TelaioCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PeriziaForm(
    val periziaInModifica: Perizia? = null,
    val targa: String = "",
    val telaio: String = "",
    val modello: String = "",
    val fotoPaths: List<String> = emptyList(),
    val errore: String? = null
) {
    val isModifica: Boolean
        get() = periziaInModifica != null
}

data class TrasferimentoUiState(
    val inCorso: Boolean = false,
    val messaggio: String? = null,
    val errore: String? = null,
    val ultimoPacchettoPath: String? = null,
    val pacchettoDaInviarePath: String? = null,
    val puliziaInCorso: Boolean = false
)

data class AllineamentoUiState(
    val inCorso: Boolean = false,
    val messaggio: String? = null,
    val errore: String? = null
)

data class PerizieUiState(
    val form: PeriziaForm = PeriziaForm(),
    val perizie: List<PeriziaConFoto> = emptyList(),
    val daInviareCount: Int = 0,
    val commesseCache: List<CommessaCache> = emptyList(),
    val telaiCache: List<TelaioCache> = emptyList(),
    val allineamento: AllineamentoUiState = AllineamentoUiState(),
    val trasferimento: TrasferimentoUiState = TrasferimentoUiState()
)

class PerizieViewModel(
    private val repository: PeriziaRepository
) : ViewModel() {
    private val form = MutableStateFlow(PeriziaForm())
    private val trasferimento = MutableStateFlow(TrasferimentoUiState())
    private val allineamento = MutableStateFlow(AllineamentoUiState())
    private val commesseCache = MutableStateFlow<List<CommessaCache>>(emptyList())

    private val dekraData = combine(
        repository.perizie,
        repository.daInviareCount,
        repository.telaiCache
    ) { perizie, daInviareCount, telaiCache ->
        Triple(perizie, daInviareCount, telaiCache)
    }

    private val baseState = combine(
        form,
        dekraData,
        commesseCache,
        allineamento
    ) { form, data, commesseCache, allineamento ->
        PerizieUiState(
            form = form,
            perizie = data.first,
            daInviareCount = data.second,
            commesseCache = commesseCache,
            telaiCache = data.third,
            allineamento = allineamento
        )
    }

    val uiState = combine(
        baseState,
        trasferimento
    ) { state, trasferimento ->
        state.copy(trasferimento = trasferimento)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PerizieUiState()
    )

    init {
        viewModelScope.launch {
            repository.prepareDekraOffline()
        }
    }

    fun preparaCacheDekraPerUtente(utenteId: String) {
        viewModelScope.launch {
            commesseCache.value = repository.preparaCacheDekraPerUtente(utenteId)
        }
    }

    fun allineaDatabase(endpoint: String, utenteId: String, password: String) {
        viewModelScope.launch {
            allineamento.value = AllineamentoUiState(inCorso = true)
            runCatching {
                repository.allineaDatabase(endpoint, utenteId, password)
            }.onSuccess { (result, commesseAggiornate) ->
                commesseCache.value = commesseAggiornate
                allineamento.value = AllineamentoUiState(
                    messaggio = "Allineamento completato: ${result.commesse} commesse, ${result.telai} telai, ${result.tipiDocumento} tipi documento."
                )
            }.onFailure { errore ->
                allineamento.value = AllineamentoUiState(
                    errore = errore.message ?: "Errore durante l'allineamento database."
                )
            }
        }
    }

    fun aggiornaTarga(value: String) {
        form.update { it.copy(targa = value, errore = null) }
    }

    fun aggiornaTelaio(value: String) {
        form.update { it.copy(telaio = value, errore = null) }
    }

    fun aggiornaModello(value: String) {
        form.update { it.copy(modello = value, errore = null) }
    }

    fun aggiungiFoto(path: String) {
        form.update { it.copy(fotoPaths = it.fotoPaths + path, errore = null) }
    }

    fun rimuoviFoto(path: String) {
        form.update { it.copy(fotoPaths = it.fotoPaths - path, errore = null) }
    }

    fun salva() {
        val current = form.value
        if (current.targa.isBlank()) {
            form.update { it.copy(errore = "La targa e' obbligatoria.") }
            return
        }

        viewModelScope.launch {
            val periziaInModifica = current.periziaInModifica
            if (periziaInModifica == null) {
                repository.salva(
                    targa = current.targa,
                    telaio = current.telaio,
                    modello = current.modello,
                    fotoPaths = current.fotoPaths
                )
            } else {
                repository.aggiorna(
                    perizia = periziaInModifica,
                    targa = current.targa,
                    telaio = current.telaio,
                    modello = current.modello,
                    fotoPaths = current.fotoPaths
                )
            }
            form.value = PeriziaForm()
        }
    }

    fun creaDekraPerizia(commessaId: Int, targa: String) {
        if (targa.isBlank()) return

        viewModelScope.launch {
            repository.creaDekraPerizia(commessaId, targa)
        }
    }

    fun creaDekraPeriziaDaTelaio(telaio: TelaioCache) {
        viewModelScope.launch {
            repository.creaDekraPeriziaDaTelaio(telaio)
        }
    }

    fun aggiungiFotoGuidata(periziaId: Long, tipoDocumento: DekraTipoDocumentoSeed, path: String) {
        viewModelScope.launch {
            repository.aggiungiFotoGuidata(periziaId, tipoDocumento, path)
        }
    }

    fun rimuoviFotoGuidata(foto: FotoPerizia) {
        viewModelScope.launch {
            repository.rimuoviFotoGuidata(foto)
        }
    }
    fun segnaCommessaDaReinviare(commessaId: Int) {
        viewModelScope.launch {
            repository.segnaCommessaDaReinviare(commessaId)
        }
    }

    fun modifica(periziaConFoto: PeriziaConFoto) {
        val perizia = periziaConFoto.perizia
        form.value = PeriziaForm(
            periziaInModifica = perizia,
            targa = perizia.targa,
            telaio = perizia.telaio,
            modello = perizia.modello,
            fotoPaths = periziaConFoto.foto.map { it.path }
        )
    }

    fun annullaModifica() {
        form.value = PeriziaForm()
    }

    fun creaPacchettoTrasferimento(context: Context) {
        viewModelScope.launch {
            trasferimento.value = TrasferimentoUiState(inCorso = true)
            runCatching {
                repository.creaPacchettoTrasferimento(context.applicationContext)
            }.onSuccess { result ->
                trasferimento.value = if (result == null) {
                    TrasferimentoUiState(
                        messaggio = "Non ci sono dati da trasferire."
                    )
                } else {
                    TrasferimentoUiState(
                        messaggio = "Pacchetto creato: ${result.perizie} perizie, ${result.foto} foto.",
                        ultimoPacchettoPath = result.file.absolutePath,
                        pacchettoDaInviarePath = result.file.absolutePath
                    )
                }
            }.onFailure { errore ->
                trasferimento.value = TrasferimentoUiState(
                    errore = errore.message ?: "Errore durante la creazione del pacchetto."
                )
            }
        }
    }

    fun inviaPacchettoApi(context: Context, endpoint: String) {
        viewModelScope.launch {
            trasferimento.value = TrasferimentoUiState(inCorso = true)
            runCatching {
                repository.inviaPacchettoApi(context.applicationContext, endpoint)
            }.onSuccess { result ->
                trasferimento.value = if (result == null) {
                    TrasferimentoUiState(
                        messaggio = "Non ci sono dati da trasferire."
                    )
                } else {
                    TrasferimentoUiState(
                        messaggio = "Import API completato: ${result.perizie} perizie, ${result.foto} foto.",
                        ultimoPacchettoPath = result.file.absolutePath
                    )
                }
            }.onFailure { errore ->
                trasferimento.value = TrasferimentoUiState(
                    errore = errore.message ?: "Errore durante l'invio all'API."
                )
            }
        }
    }

    fun pacchettoEmailAperto() {
        trasferimento.update { it.copy(pacchettoDaInviarePath = null) }
    }
    fun pulisciFotoInviate() {
        viewModelScope.launch {
            trasferimento.update { it.copy(puliziaInCorso = true, messaggio = null, errore = null) }
            runCatching {
                repository.pulisciFotoInviate()
            }.onSuccess { result ->
                trasferimento.update {
                    it.copy(
                        puliziaInCorso = false,
                        messaggio = "Cancellate ${result.perizieCancellate} perizie inviate e ${result.fotoCancellate} foto.",
                        errore = null
                    )
                }
            }.onFailure { errore ->
                trasferimento.update {
                    it.copy(
                        puliziaInCorso = false,
                        errore = errore.message ?: "Errore durante la cancellazione dei dati inviati."
                    )
                }
            }
        }
    }

    fun elimina(perizia: Perizia) {
        viewModelScope.launch {
            repository.elimina(perizia)
            if (form.value.periziaInModifica?.id == perizia.id) {
                form.value = PeriziaForm()
            }
        }
    }
}

class PerizieViewModelFactory(
    private val repository: PeriziaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerizieViewModel::class.java)) {
            return PerizieViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel non supportato: ${modelClass.name}")
    }
}
