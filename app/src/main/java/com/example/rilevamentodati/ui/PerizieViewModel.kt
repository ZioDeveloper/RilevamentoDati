package com.example.rilevamentodati.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rilevamentodati.data.DekraTipoDocumentoSeed
import com.example.rilevamentodati.data.FotoPerizia
import com.example.rilevamentodati.data.Perizia
import com.example.rilevamentodati.data.PeriziaConFoto
import com.example.rilevamentodati.data.PeriziaRepository
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

data class PerizieUiState(
    val form: PeriziaForm = PeriziaForm(),
    val perizie: List<PeriziaConFoto> = emptyList(),
    val daInviareCount: Int = 0
)

class PerizieViewModel(
    private val repository: PeriziaRepository
) : ViewModel() {
    private val form = MutableStateFlow(PeriziaForm())

    val uiState = combine(
        form,
        repository.perizie,
        repository.daInviareCount
    ) { form, perizie, daInviareCount ->
        PerizieUiState(
            form = form,
            perizie = perizie,
            daInviareCount = daInviareCount
        )
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
            form.update {
                it.copy(errore = "La targa e' obbligatoria.")
            }
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

    fun inviaDati() {
        viewModelScope.launch {
            repository.inviaDati()
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

