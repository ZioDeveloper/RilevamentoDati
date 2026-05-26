package com.example.rilevamentodati

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.media.ExifInterface
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.FileProvider
import com.example.rilevamentodati.data.DekraCommessaSeed
import com.example.rilevamentodati.data.DekraSeedData
import com.example.rilevamentodati.data.DekraTipoDocumentoSeed
import com.example.rilevamentodati.data.FotoPerizia
import com.example.rilevamentodati.data.Perizia
import com.example.rilevamentodati.data.PeriziaConFoto
import com.example.rilevamentodati.data.SyncStatus
import com.example.rilevamentodati.ui.PerizieUiState
import com.example.rilevamentodati.ui.PerizieViewModel
import com.example.rilevamentodati.ui.PerizieViewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RilevamentoDatiTheme {
                val app = LocalContext.current.applicationContext as RilevamentoDatiApp
                val viewModel: PerizieViewModel = viewModel(
                    factory = PerizieViewModelFactory(app.repository)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                var utenteIdAutenticato by rememberSaveable { mutableStateOf<String?>(null) }
                val utenteAutenticato = remember(utenteIdAutenticato) {
                    utenteIdAutenticato?.let(::trovaUtenteLogin)
                }

                if (utenteAutenticato == null) {
                    LoginScreen(
                        onLoginSuccess = { utente -> utenteIdAutenticato = utente.id },
                        onCloseAppClick = { finish() }
                    )
                } else {
                    RilevamentoDatiAppScreen(
                        state = state,
                        utente = utenteAutenticato,
                        onLogoutClick = { utenteIdAutenticato = null },
                        onCloseAppClick = { finish() },
                        onTargaChange = viewModel::aggiornaTarga,
                        onTelaioChange = viewModel::aggiornaTelaio,
                        onModelloChange = viewModel::aggiornaModello,
                        onAggiungiFoto = viewModel::aggiungiFoto,
                        onRimuoviFoto = viewModel::rimuoviFoto,
                        onCreaDekraPerizia = viewModel::creaDekraPerizia,
                        onAggiungiFotoGuidata = viewModel::aggiungiFotoGuidata,
                        onRimuoviFotoGuidata = viewModel::rimuoviFotoGuidata,
                        onSegnaCommessaDaReinviare = viewModel::segnaCommessaDaReinviare,
                        onSalvaClick = viewModel::salva,
                        onInviaDatiClick = { viewModel.inviaPacchettoApi(app, API_IMPORT_URL) },
                        onPacchettoEmailAperto = viewModel::pacchettoEmailAperto,
                        onPulisciFotoInviateClick = viewModel::pulisciFotoInviate,
                        onModificaClick = viewModel::modifica,
                        onAnnullaModificaClick = viewModel::annullaModifica,
                        onEliminaClick = viewModel::elimina
                    )
                }
            }
        }
    }
}

private data class LoginUser(
    val id: String,
    val nome: String,
    val cognome: String,
    val password: String,
    val idClasse: Int
)

private val utentiLogin = listOf(
    LoginUser("ADP", "ANDREA DE PAOLIS", "DE PAOLIS", "NSG26", 1),
    LoginUser("ADR", "A NOME", "A COGNOME", "NSG", 3),
    LoginUser("ADRI", "ABI", "ABI", "NSG25", 3),
    LoginUser("ADV", "ANTONIO 24", "MARINO", "NSG25", 3),
    LoginUser("ALU", "ANDREA LUCHI", "LUCHI", "NSG26", 1),
    LoginUser("BEN", "FRANCESCO", "DE BENEDICTIS", "NSG", 3),
    LoginUser("C001", "Fabio", "Vezina", "Alesi", 1),
    LoginUser("CAPM", "MARCO C", "CAPORALI", "NSG26", 3),
    LoginUser("CHI", "LUCA CHI", "CHIAZZOLINO", "NSG25", 3),
    LoginUser("CHS", "ENZO", "CHIESI", "NSG26", 1),
    LoginUser("CIRO", "CIRO", "Canistro", "NSG", 3),
    LoginUser("DAM", "Service", "Clinicar", "NSG", 3),
    LoginUser("DEB", "", "De Bona", "Deb@2020", 4),
    LoginUser("DEL", "RICCARDO D", "DELFINO", "VARAZZE", 1),
    LoginUser("DIO", "DIOGO", "DIOGO", "NSG", 3),
    LoginUser("DMA", "Lorenzo", "De Martiis", "NSG", 1),
    LoginUser("DOU", "DOUGLAS WILLIAN", "ARAUJO CARNEIRO", "NSG25", 3),
    LoginUser("EDE", "EDERSON", "Ednilson", "NSG", 3),
    LoginUser("EDI", "EDINHO", "EDINHO", "NSG", 3),
    LoginUser("EDU", "EDUARDO", "EDUARDO", "NSG", 3),
    LoginUser("ENK", "ENKI", "ENKI", "NSG25", 3),
    LoginUser("FAC", "FACETO", "FACETO", "NSG", 3),
    LoginUser("FIE", "GIANLUCA F", "FIERAMOSCA", "NSG26", 1),
    LoginUser("FRND", "FERNANDINHO", "SANCEZ", "N01", 2),
    LoginUser("GAN", "GEOVANNE", "BEZERRA DE LIMA", "NSG25", 3),
    LoginUser("GES", "Antonio g", "Gesualdi", "NSG25", 3),
    LoginUser("GOT", "Luca G", "Gotti", "Nsg25", 3),
    LoginUser("GUS", "GUSTAVO", "GUSTAVO", "NSG", 3),
    LoginUser("HUG", "HUGO", "MONTOYA", "NSG", 3),
    LoginUser("IOV", "IOVINO CARMELA", "", "NSG26", 1),
    LoginUser("ISO", "ISOLI", ".", "NSG", 2),
    LoginUser("IVE", "IVECO", "GIOVANNI MECHI", "NSG", 4),
    LoginUser("JAIR", "JAIR SANTOS", "JAIR SANTOS", "NSG", 3),
    LoginUser("JAO", "JOAO", "JOAO", "NSG", 3),
    LoginUser("JOA", "JOAS", "LOPES FERREIRA", "NSG25", 3),
    LoginUser("JOB", "JULIANO BRUXO", "OLIVEIRA", "NSG", 3),
    LoginUser("JONA", "JONATAN", "JONATAN", "NSG", 3),
    LoginUser("JUL", "JULIANO 23", "PRESOTTO", "NSG", 3),
    LoginUser("JUNR", "JUNIOR", "JUNIOR", "NSG", 3),
    LoginUser("L001", "MANUEL", "Longoni", "Luglio2020", 1),
    LoginUser("L002", "Jorge", "DAMIANI", "Luglio2020", 3),
    LoginUser("L003", "RAFAEL", "ALVES", "Sette", 3),
    LoginUser("L005", "Maurizio", "Scavone", "NSG25", 2),
    LoginUser("L006", "Manuel T", "Tettamanti", "Luglio2021", 3),
    LoginUser("LEO", "LEOPOLDO", "Canistro L", "NSG", 2),
    LoginUser("LONJ", "RICCARDO LONGONI", "RICCARDO LONGONI", "JUVE", 3),
    LoginUser("LPE", "Luca", "Peaquin", "Luglio2021", 3),
    LoginUser("MDP", "Michele", "Longo", "NSG25", 3),
    LoginUser("MEY", "ANDREA MEY", "MEY CHIAZZ", "NSG25", 3),
    LoginUser("MRC", "MARCELO 23", "BONATTO", "NSG", 3),
    LoginUser("NICO", "NICO", "ALBANESE", "NSG25", 3),
    LoginUser("PASQ", "PASQUALE", "Pasquale/Luigi", "NSG25", 3),
    LoginUser("RGD", "Ricardo Gabriel", "Diaz", "NSG", 3),
    LoginUser("RIV", "NICOLO'", "RIVOLO", "CERES", 3),
    LoginUser("ROBS", "ROBSON", "ROBSON", "NSG", 3),
    LoginUser("ROBY", "ROBERTO C", "CORDIO", "NSG", 3),
    LoginUser("RUS", "Franco", "Russo", "NSG25", 3),
    LoginUser("RUSA", "Andrea", "Russo", "NSG25", 3),
    LoginUser("RUSS", "Donato", "Russo", "NSG", 3),
    LoginUser("SEL", "Costantino", "Sellitto", "NSG", 3),
    LoginUser("SSS", "Alessio", "Sessa", "Luglio2021", 3),
    LoginUser("TET", "Manuel T", "Tettamanti", "NSG25", 3),
    LoginUser("TTT", "Tommaso", "Guerra", "NSG24", 3),
    LoginUser("VAN", "VALDIS", "CAPISTRANO DE LIMA", "NSG25", 3),
    LoginUser("VAS", "RINALDO", "VIEIRA DE MELO JUNIOR", "NSG25", 3),
    LoginUser("VLM", "Ricky Longoni", "Longoni R", "NSG", 3),
    LoginUser("VLR", "Valeriano", "Siragusa", "NSG25", 1),
    LoginUser("VZS", "VEZINA SIMONE TECNICO", "VEZZA", "NSG25", 3),
    LoginUser("ZAN", "DANIELE", "ZANELLATO", "NSG26", 4),
    LoginUser("ZUC", "MANUEL Z", "ZUCCHETTI", "NSG25", 3)
)
private fun trovaUtenteLogin(id: String): LoginUser? {
    val codice = id.trim().uppercase()
    return utentiLogin.firstOrNull { it.id == codice }
}

private fun autenticaUtente(id: String, password: String): LoginUser? {
    val codice = id.trim().uppercase()
    val passwordInserita = password.trim()
    return utentiLogin.firstOrNull {
        it.id == codice && it.password.equals(passwordInserita, ignoreCase = true)
    }
}

private val AppColorScheme = lightColorScheme(
    primary = Color(0xFF1F6F5B),
    onPrimary = Color.White,
    secondary = Color(0xFF8A5A00),
    tertiary = Color(0xFF315D75),
    background = Color(0xFFF6F7F4),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7ECE7),
    error = Color(0xFFBA1A1A)
)

@Composable
private fun RilevamentoDatiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}

@Composable
private fun LoginScreen(
    onLoginSuccess: (LoginUser) -> Unit,
    onCloseAppClick: () -> Unit
) {
    var codice by rememberSaveable { mutableStateOf("C001") }
    var password by rememberSaveable { mutableStateOf("Alesi") }
    var errore by rememberSaveable { mutableStateOf<String?>(null) }
    val isOnline = rememberOnlineStatus()
    val appVersion = rememberAppVersionName()
    val submitLogin = {
        val utente = autenticaUtente(codice, password)
        if (utente == null) {
            errore = "Codice o password non validi."
        } else {
            onLoginSuccess(utente)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nonsolograndine_logo_orizzontale),
                        contentDescription = "Non Solo Grandine",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp),
                        contentScale = ContentScale.Fit
                    )

                    SectionTitle("Accesso operatore")

                    OnlineStatusCard(isOnline = isOnline)

                    OutlinedTextField(
                        value = codice,
                        onValueChange = {
                            codice = it.uppercase()
                            errore = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Codice") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errore = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submitLogin() }
                        )
                    )

                    errore?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = submitLogin,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Accedi")
                    }

                    OutlinedButton(
                        onClick = onCloseAppClick,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Chiudi app")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Versione $appVersion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private enum class AppSection(
    val title: String
) {
    DEKRA("DEKRA offline"),
    TRASFERIMENTO("Trasferimento dati")
}

private const val DETAIL_NEW = "new"
private const val DETAIL_EDIT = "edit"
private const val API_IMPORT_URL = "http://127.0.0.1:5205/api/import/rilevamento-dati?dryRun=false"
private const val DESTINATARIO_TRASFERIMENTO = ""
private const val FOTO_TARGET_MAX_BYTES = 300 * 1024
private const val FOTO_MAX_DIMENSION = 1600
private const val FOTO_MIN_DIMENSION = 900

private data class FotoGuidataPending(
    val periziaId: Long,
    val tipoDocumento: DekraTipoDocumentoSeed
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RilevamentoDatiAppScreen(
    state: PerizieUiState,
    utente: LoginUser,
    onLogoutClick: () -> Unit,
    onCloseAppClick: () -> Unit,
    onTargaChange: (String) -> Unit,
    onTelaioChange: (String) -> Unit,
    onModelloChange: (String) -> Unit,
    onAggiungiFoto: (String) -> Unit,
    onRimuoviFoto: (String) -> Unit,
    onCreaDekraPerizia: (Int, String) -> Unit,
    onAggiungiFotoGuidata: (Long, DekraTipoDocumentoSeed, String) -> Unit,
    onRimuoviFotoGuidata: (FotoPerizia) -> Unit,
    onSegnaCommessaDaReinviare: (Int) -> Unit,
    onSalvaClick: () -> Unit,
    onInviaDatiClick: () -> Unit,
    onPacchettoEmailAperto: () -> Unit,
    onPulisciFotoInviateClick: () -> Unit,
    onModificaClick: (PeriziaConFoto) -> Unit,
    onAnnullaModificaClick: () -> Unit,
    onEliminaClick: (Perizia) -> Unit
) {
    var currentSectionName by rememberSaveable { mutableStateOf(AppSection.DEKRA.name) }
    var detailMode by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPeriziaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedDekraCommessaId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedDekraPeriziaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var nuovaTargaCommessaId by rememberSaveable { mutableStateOf<Int?>(null) }
    var nuovaTarga by rememberSaveable { mutableStateOf("") }
    var nuovaTargaErrore by rememberSaveable { mutableStateOf<String?>(null) }
    var targaDaAprireDopoCreazione by rememberSaveable { mutableStateOf<String?>(null) }
    var mostraConfermaInvio by remember { mutableStateOf(false) }
    val currentSection = AppSection.valueOf(currentSectionName)
    val selectedPerizia = selectedPeriziaId?.let { id ->
        state.perizie.firstOrNull { it.perizia.id == id }
    }
    val selectedDekraPerizia = selectedDekraPeriziaId?.let { id ->
        state.perizie.firstOrNull { it.perizia.id == id }
    }
    LaunchedEffect(state.perizie, targaDaAprireDopoCreazione, selectedDekraCommessaId) {
        val targaCreata = targaDaAprireDopoCreazione ?: return@LaunchedEffect
        val commessaId = selectedDekraCommessaId ?: return@LaunchedEffect
        val nuovaPerizia = state.perizie.firstOrNull {
            it.perizia.isDekra &&
                it.perizia.idCommessa == commessaId &&
                it.perizia.idTelaioOrigine == null &&
                it.perizia.targa == targaCreata
        }
        if (nuovaPerizia != null) {
            selectedDekraPeriziaId = nuovaPerizia.perizia.id
            targaDaAprireDopoCreazione = null
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var fotoInScattoPath by remember { mutableStateOf<String?>(null) }
    var fotoGuidataInScatto by remember { mutableStateOf<FotoGuidataPending?>(null) }
    var fotoGuidataDaGalleria by remember { mutableStateOf<FotoGuidataPending?>(null) }
    val context = LocalContext.current
    val isOnline = rememberOnlineStatus()
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val path = fotoInScattoPath
        val fotoGuidata = fotoGuidataInScatto
        if (success && path != null && fotoGuidata != null) {
            ottimizzaFotoPerInvio(File(path))
            onAggiungiFotoGuidata(fotoGuidata.periziaId, fotoGuidata.tipoDocumento, path)
        } else if (success && path != null) {
            ottimizzaFotoPerInvio(File(path))
            onAggiungiFoto(path)
        } else if (path != null) {
            File(path).delete()
        }
        fotoGuidataInScatto = null
        fotoInScattoPath = null
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val fotoGuidata = fotoGuidataDaGalleria
        uri?.let {
            copyPhotoToAppStorage(context, it)?.let { path ->
                if (fotoGuidata != null) {
                    onAggiungiFotoGuidata(fotoGuidata.periziaId, fotoGuidata.tipoDocumento, path)
                } else {
                    onAggiungiFoto(path)
                }
            }
        }
        fotoGuidataDaGalleria = null
    }

    fun tornaAllaLista() {
        currentSectionName = AppSection.DEKRA.name
        detailMode = null
        selectedPeriziaId = null
        selectedDekraCommessaId = null
        selectedDekraPeriziaId = null
        nuovaTargaCommessaId = null
        nuovaTarga = ""
        nuovaTargaErrore = null
        targaDaAprireDopoCreazione = null
        onAnnullaModificaClick()
    }

    fun apriSezione(section: AppSection) {
        currentSectionName = section.name
        detailMode = null
        selectedPeriziaId = null
        selectedDekraCommessaId = null
        selectedDekraPeriziaId = null
        nuovaTargaCommessaId = null
        nuovaTarga = ""
        nuovaTargaErrore = null
        targaDaAprireDopoCreazione = null
        onAnnullaModificaClick()
        scope.launch { drawerState.close() }
    }

    val inDettaglio = detailMode != null
    val inDekraCommessa = currentSection == AppSection.DEKRA && selectedDekraCommessaId != null
    val inDekraVeicolo = inDekraCommessa && selectedDekraPeriziaId != null
    val mostraIndietro = inDettaglio || inDekraCommessa
    val titolo = when {
        inDekraVeicolo -> "Foto DEKRA"
        inDekraCommessa -> "Veicoli DEKRA"
        detailMode == DETAIL_NEW -> "Nuova perizia"
        detailMode == DETAIL_EDIT -> "Dettaglio perizia"
        else -> currentSection.title
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentSection = currentSection,
                inDettaglio = inDettaglio || inDekraCommessa,
                utente = utente,
                onSectionClick = { section -> apriSezione(section) },
                onCloseAppClick = onCloseAppClick
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (mostraIndietro) {
                            IconButton(
                                onClick = {
                                    when {
                                        inDekraVeicolo -> selectedDekraPeriziaId = null
                                        inDekraCommessa -> selectedDekraCommessaId = null
                                        else -> tornaAllaLista()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Torna indietro"
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    },
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(titolo)
                            Text(
                                text = "${utente.nome} ${utente.cognome} - ${utente.id}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = onLogoutClick) {
                            Text("Esci")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            when {
                inDettaglio -> {
                    PeriziaDetailScreen(
                        state = state,
                        periziaInDettaglio = selectedPerizia?.perizia,
                        onTargaChange = onTargaChange,
                        onTelaioChange = onTelaioChange,
                        onModelloChange = onModelloChange,
                        onScattaFotoClick = {
                            val file = createPhotoFile(context)
                            fotoInScattoPath = file.absolutePath
                            cameraLauncher.launch(createPhotoUri(context, file))
                        },
                        onScegliFotoClick = { photoPickerLauncher.launch("image/*") },
                        onRimuoviFotoClick = onRimuoviFoto,
                        onSalvaClick = {
                            val chiudiDopoSalvataggio = state.form.targa.isNotBlank()
                            onSalvaClick()
                            if (chiudiDopoSalvataggio) {
                                tornaAllaLista()
                            }
                        },
                        onAnnullaClick = { tornaAllaLista() },
                        onEliminaClick = { perizia ->
                            onEliminaClick(perizia)
                            tornaAllaLista()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }


                currentSection == AppSection.DEKRA -> {
                    val commessa = DekraSeedData.commessa(selectedDekraCommessaId)
                    when {
                        commessa == null -> {
                            DekraCommesseScreen(
                                utente = utente,
                                isOnline = isOnline,
                                onCommessaClick = { selectedDekraCommessaId = it.id },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        selectedDekraPerizia == null -> {
                            DekraVeicoliScreen(
                                commessa = commessa,
                                perizie = state.perizie.filter {
                                    it.perizia.isDekra && it.perizia.idCommessa == commessa.id
                                },
                                onNuovaPeriziaClick = {
                                    nuovaTargaCommessaId = commessa.id
                                    nuovaTarga = ""
                                    nuovaTargaErrore = null
                                },
                                onPeriziaClick = { selectedDekraPeriziaId = it.perizia.id },
                                onSegnaDaReinviareClick = { onSegnaCommessaDaReinviare(commessa.id) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        else -> {
                            DekraFotoGuidataScreen(
                                periziaConFoto = selectedDekraPerizia,
                                onScattaFotoClick = { tipoDocumento ->
                                    val file = createPhotoFile(context)
                                    fotoGuidataInScatto = FotoGuidataPending(
                                        periziaId = selectedDekraPerizia.perizia.id,
                                        tipoDocumento = tipoDocumento
                                    )
                                    fotoInScattoPath = file.absolutePath
                                    cameraLauncher.launch(createPhotoUri(context, file))
                                },
                                onScegliFotoClick = { tipoDocumento ->
                                    fotoGuidataDaGalleria = FotoGuidataPending(
                                        periziaId = selectedDekraPerizia.perizia.id,
                                        tipoDocumento = tipoDocumento
                                    )
                                    photoPickerLauncher.launch("image/*")
                                },
                                onRimuoviFotoClick = onRimuoviFotoGuidata,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }

                currentSection == AppSection.TRASFERIMENTO -> {
                    TrasferimentoDatiScreen(
                        state = state,
                        isOnline = isOnline,
                        onInviaDatiClick = { mostraConfermaInvio = true },
                        onPacchettoEmailAperto = onPacchettoEmailAperto,
                        onPulisciFotoInviateClick = onPulisciFotoInviateClick,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    nuovaTargaCommessaId?.let { commessaId ->
        DekraSeedData.commessa(commessaId)?.let { commessa ->
            NuovaDekraPeriziaDialog(
                commessa = commessa,
                targa = nuovaTarga,
                errore = nuovaTargaErrore,
                onTargaChange = {
                    nuovaTarga = it.uppercase()
                    nuovaTargaErrore = null
                },
                onConfirm = {
                    val targaPulita = nuovaTarga.trim().uppercase()
                    if (targaPulita.isBlank()) {
                        nuovaTargaErrore = "La targa e' obbligatoria."
                    } else {
                        onCreaDekraPerizia(commessa.id, targaPulita)
                        targaDaAprireDopoCreazione = targaPulita
                        nuovaTargaCommessaId = null
                        nuovaTarga = ""
                        nuovaTargaErrore = null
                    }
                },
                onDismiss = {
                    nuovaTargaCommessaId = null
                    nuovaTarga = ""
                    nuovaTargaErrore = null
                }
            )
        }
    }

    if (mostraConfermaInvio) {
        ConfirmDialog(
            title = "Creare il pacchetto dati?",
            text = "Verranno raccolte in un file ZIP ${state.daInviareCount} perizie con le relative foto e si aprira' l'app email per l'invio.",
            confirmText = "Crea e invia email",
            onConfirm = {
                mostraConfermaInvio = false
                if (isOnline) {
                    onInviaDatiClick()
                }
            },
            onDismiss = { mostraConfermaInvio = false }
        )
    }
}

@Composable
private fun AppDrawer(
    currentSection: AppSection,
    inDettaglio: Boolean,
    utente: LoginUser,
    onSectionClick: (AppSection) -> Unit,
    onCloseAppClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Rilevamento Dati",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${utente.nome} ${utente.cognome}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "${utente.id} - Classe ${utente.idClasse}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()


        NavigationDrawerItem(
            label = { Text(AppSection.DEKRA.title) },
            selected = currentSection == AppSection.DEKRA && !inDettaglio,
            onClick = { onSectionClick(AppSection.DEKRA) },
            icon = {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        NavigationDrawerItem(
            label = { Text(AppSection.TRASFERIMENTO.title) },
            selected = currentSection == AppSection.TRASFERIMENTO && !inDettaglio,
            onClick = { onSectionClick(AppSection.TRASFERIMENTO) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text("Chiudi app") },
            selected = false,
            onClick = onCloseAppClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DekraCommesseScreen(
    utente: LoginUser,
    isOnline: Boolean,
    onCommessaClick: (DekraCommessaSeed) -> Unit,
    modifier: Modifier = Modifier
) {
    val commesse = remember(utente.id) { DekraSeedData.commessePerUtente(utente.id) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle("Commesse DEKRA")
        }

        item {
            OnlineStatusCard(isOnline = isOnline)
        }

        if (commesse.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Nessuna commessa DEKRA associata all'utente.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(
                items = commesse,
                key = { it.id }
            ) { commessa ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCommessaClick(commessa) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = commessa.descrizione,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${commessa.codice} - ID ${commessa.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DekraVeicoliScreen(
    commessa: DekraCommessaSeed,
    perizie: List<PeriziaConFoto>,
    onNuovaPeriziaClick: () -> Unit,
    onPeriziaClick: (PeriziaConFoto) -> Unit,
    onSegnaDaReinviareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inviate = perizie.count { it.perizia.syncStatus == SyncStatus.INVIATO }
    val daInviare = perizie.count { it.perizia.syncStatus == SyncStatus.DA_INVIARE }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(commessa.descrizione)
                Text(
                    text = "${perizie.size} veicoli disponibili offline - $daInviare da inviare, $inviate inviati",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedButton(
                    onClick = onSegnaDaReinviareClick,
                    enabled = inviate > 0,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        if (inviate > 0) {
                            "Segna da reinviare ($inviate)"
                        } else {
                            "Nessuna perizia inviata da reinviare"
                        }
                    )
                }
                Button(
                    onClick = onNuovaPeriziaClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Nuova targa")
                }
            }
        }

        if (perizie.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Non ci sono ancora veicoli offline per questa commessa.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(
                items = perizie,
                key = { it.perizia.id }
            ) { perizia ->
                DekraVeicoloItem(
                    periziaConFoto = perizia,
                    onClick = { onPeriziaClick(perizia) }
                )
            }
        }
    }
}

@Composable
private fun DekraVeicoloItem(
    periziaConFoto: PeriziaConFoto,
    onClick: () -> Unit
) {
    val perizia = periziaConFoto.perizia
    val completed = completedDekraSteps(periziaConFoto)
    val total = DekraSeedData.tipiDocumentoGuidati.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = perizia.targa,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (perizia.modello.isNotBlank()) {
                        Text(
                            text = perizia.modello,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Text(
                    text = "$completed/$total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (perizia.telaio.isNotBlank()) {
                Text(
                    text = "Telaio: ${perizia.telaio}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            perizia.idTelaioOrigine?.let { idTelaioOrigine ->
                Text(
                    text = "ID telaio Grandine: $idTelaioOrigine",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun DekraFotoGuidataScreen(
    periziaConFoto: PeriziaConFoto,
    onScattaFotoClick: (DekraTipoDocumentoSeed) -> Unit,
    onScegliFotoClick: (DekraTipoDocumentoSeed) -> Unit,
    onRimuoviFotoClick: (FotoPerizia) -> Unit,
    modifier: Modifier = Modifier
) {
    val perizia = periziaConFoto.perizia
    val tipi = DekraSeedData.tipiDocumentoGuidati
    val tipiExtra = DekraSeedData.tipiDocumentoExtra
    val completed = completedDekraSteps(periziaConFoto)
    val current = currentDekraStep(periziaConFoto)
    var extraTipoDocumentoId by rememberSaveable(perizia.id) {
        mutableStateOf(DekraSeedData.tipoDocumentoDanni().id)
    }
    var mostraSceltaTipoExtra by remember { mutableStateOf(false) }
    val extraTipoDocumento = DekraSeedData.tipoDocumento(extraTipoDocumentoId)
        ?: DekraSeedData.tipoDocumentoDanni()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = perizia.targa,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (perizia.modello.isNotBlank()) {
                        Text(
                            text = perizia.modello,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (perizia.telaio.isNotBlank()) {
                        Text(
                            text = "Telaio: ${perizia.telaio}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Foto obbligatorie: $completed/${tipi.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (current == null) {
                        SectionTitle("Sequenza completata")
                        Text(
                            text = "Tutte le foto obbligatorie DEKRA sono state acquisite.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Puoi continuare a scattare altre foto scegliendo il tipo documento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Tipo foto selezionato",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = extraTipoDocumento.descrizione,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                OutlinedButton(
                                    onClick = { mostraSceltaTipoExtra = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                                ) {
                                    Text("Cambia tipo foto")
                                }
                            }
                        }

                        Button(
                            onClick = { onScattaFotoClick(extraTipoDocumento) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Scatta foto")
                        }
                        OutlinedButton(
                            onClick = { onScegliFotoClick(extraTipoDocumento) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Scegli foto esistente")
                        }
                    } else {
                        SectionTitle("Prossima foto")
                        Text(
                            text = "${current.ordinePerizia}/${tipi.size} - ${current.descrizione}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Foto richieste: ${current.numMinFoto}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Button(
                            onClick = { onScattaFotoClick(current) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Scatta foto")
                        }
                        OutlinedButton(
                            onClick = { onScegliFotoClick(current) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Scegli foto esistente")
                        }
                    }
                }
            }
        }

        item {
            SectionTitle("Sequenza foto")
        }

        items(
            items = tipi,
            key = { it.id }
        ) { tipo ->
            DekraStepCard(
                tipoDocumento = tipo,
                foto = periziaConFoto.foto.filter { it.tipoDocumentoId == tipo.id },
                onRimuoviFotoClick = onRimuoviFotoClick
            )
        }

        val tipiExtraNonGuidati = tipiExtra.filter { extra -> tipi.none { it.id == extra.id } }
        if (tipiExtraNonGuidati.isNotEmpty()) {
            item {
                SectionTitle("Altre foto")
            }
            items(
                items = tipiExtraNonGuidati,
                key = { it.id }
            ) { tipo ->
                DekraStepCard(
                    tipoDocumento = tipo,
                    foto = periziaConFoto.foto.filter { it.tipoDocumentoId == tipo.id },
                    onRimuoviFotoClick = onRimuoviFotoClick,
                    obbligatoria = false
                )
            }
        }
    }

    if (mostraSceltaTipoExtra) {
        TipoDocumentoDialog(
            tipi = tipiExtra,
            selectedId = extraTipoDocumento.id,
            onSelect = { tipo ->
                extraTipoDocumentoId = tipo.id
                mostraSceltaTipoExtra = false
            },
            onDismiss = { mostraSceltaTipoExtra = false }
        )
    }
}

@Composable
private fun DekraStepCard(
    tipoDocumento: DekraTipoDocumentoSeed,
    foto: List<FotoPerizia>,
    onRimuoviFotoClick: (FotoPerizia) -> Unit,
    obbligatoria: Boolean = true
) {
    val completata = !obbligatoria || foto.size >= tipoDocumento.numMinFoto

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (obbligatoria) {
                            "${tipoDocumento.ordinePerizia}. ${tipoDocumento.descrizione}"
                        } else {
                            tipoDocumento.descrizione
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (obbligatoria) {
                            "${foto.size}/${tipoDocumento.numMinFoto} foto"
                        } else {
                            "${foto.size} foto"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        !obbligatoria -> MaterialTheme.colorScheme.surfaceVariant
                        completata -> Color(0xFFD8F3DC)
                        else -> Color(0xFFFFE7B3)
                    }
                ) {
                    Text(
                        text = when {
                            !obbligatoria -> "Opzionale"
                            completata -> "OK"
                            else -> "Da fare"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            !obbligatoria -> MaterialTheme.colorScheme.onSurfaceVariant
                            completata -> Color(0xFF0B5D1E)
                            else -> Color(0xFF684800)
                        }
                    )
                }
            }

            foto.forEachIndexed { index, item ->
                Text(
                    text = "Foto ${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                PhotoPreview(
                    fotoPath = item.path,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                OutlinedButton(
                    onClick = { onRimuoviFotoClick(item) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Rimuovi foto",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TipoDocumentoDialog(
    tipi: List<DekraTipoDocumentoSeed>,
    selectedId: Int,
    onSelect: (DekraTipoDocumentoSeed) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tipo foto") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = tipi,
                    key = { it.id }
                ) { tipo ->
                    OutlinedButton(
                        onClick = { onSelect(tipo) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (tipo.id == selectedId) {
                                "${tipo.descrizione} - selezionato"
                            } else {
                                tipo.descrizione
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

private fun completedDekraSteps(periziaConFoto: PeriziaConFoto): Int {
    return DekraSeedData.tipiDocumentoGuidati.count { tipo ->
        periziaConFoto.foto.count { it.tipoDocumentoId == tipo.id } >= tipo.numMinFoto
    }
}

private fun currentDekraStep(periziaConFoto: PeriziaConFoto): DekraTipoDocumentoSeed? {
    return DekraSeedData.tipiDocumentoGuidati.firstOrNull { tipo ->
        periziaConFoto.foto.count { it.tipoDocumentoId == tipo.id } < tipo.numMinFoto
    }
}

@Composable
private fun PeriziaDetailScreen(
    state: PerizieUiState,
    periziaInDettaglio: Perizia?,
    onTargaChange: (String) -> Unit,
    onTelaioChange: (String) -> Unit,
    onModelloChange: (String) -> Unit,
    onScattaFotoClick: () -> Unit,
    onScegliFotoClick: () -> Unit,
    onRimuoviFotoClick: (String) -> Unit,
    onSalvaClick: () -> Unit,
    onAnnullaClick: () -> Unit,
    onEliminaClick: (Perizia) -> Unit,
    modifier: Modifier = Modifier
) {
    var periziaDaEliminare by remember { mutableStateOf<Perizia?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PeriziaFormCard(
                state = state,
                onTargaChange = onTargaChange,
                onTelaioChange = onTelaioChange,
                onModelloChange = onModelloChange,
                onScattaFotoClick = onScattaFotoClick,
                onScegliFotoClick = onScegliFotoClick,
                onRimuoviFotoClick = onRimuoviFotoClick,
                onSalvaClick = onSalvaClick,
                onAnnullaModificaClick = onAnnullaClick
            )
        }

        if (periziaInDettaglio != null) {
            item {
                OutlinedButton(
                    onClick = { periziaDaEliminare = periziaInDettaglio },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Elimina perizia",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    periziaDaEliminare?.let { perizia ->
        ConfirmDialog(
            title = "Cancellare la perizia?",
            text = "La perizia con targa ${perizia.targa} verra' rimossa dal dispositivo.",
            confirmText = "Cancella",
            onConfirm = {
                periziaDaEliminare = null
                onEliminaClick(perizia)
            },
            onDismiss = { periziaDaEliminare = null }
        )
    }
}

@Composable
private fun TrasferimentoDatiScreen(
    state: PerizieUiState,
    isOnline: Boolean,
    onInviaDatiClick: () -> Unit,
    onPacchettoEmailAperto: () -> Unit,
    onPulisciFotoInviateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inviati = state.perizie.count { it.perizia.syncStatus == SyncStatus.INVIATO }
    val errori = state.perizie.count { it.perizia.syncStatus == SyncStatus.ERRORE }
    val fotoInviate = state.perizie.filter { it.perizia.syncStatus == SyncStatus.INVIATO }.sumOf { it.foto.size }
    val perizieInviate = state.perizie.count { it.perizia.syncStatus == SyncStatus.INVIATO }
    var mostraConfermaPulizia by remember { mutableStateOf(false) }
    LaunchedEffect(state.trasferimento.pacchettoDaInviarePath, isOnline) {
        val path = state.trasferimento.pacchettoDaInviarePath
        if (path != null && isOnline) {
            inviaPacchettoTrasferimentoEmail(context, path)
            onPacchettoEmailAperto()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OnlineStatusCard(isOnline = isOnline)
        }

        item {
            SummarySection(
                total = state.perizie.size,
                daInviare = state.daInviareCount
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionTitle("Trasferimento dati")

                    TransferStatusRow(
                        label = "Da inviare",
                        value = state.daInviareCount.toString(),
                        status = SyncStatus.DA_INVIARE
                    )
                    TransferStatusRow(
                        label = "Inviate",
                        value = inviati.toString(),
                        status = SyncStatus.INVIATO
                    )
                    TransferStatusRow(
                        label = "Errori",
                        value = errori.toString(),
                        status = SyncStatus.ERRORE
                    )

                    state.trasferimento.messaggio?.let { messaggio ->
                        Text(
                            text = messaggio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    state.trasferimento.errore?.let { errore ->
                        Text(
                            text = errore,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    state.trasferimento.ultimoPacchettoPath?.let { path ->
                        Text(
                            text = "Ultimo pacchetto:\n$path",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { inviaPacchettoTrasferimentoEmail(context, path) },
                            enabled = isOnline,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Invia email")
                        }
                    }

                    Button(
                        onClick = onInviaDatiClick,
                        enabled = isOnline && state.daInviareCount > 0 && !state.trasferimento.inCorso,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            when {
                                state.trasferimento.inCorso -> "Invio all'API..."
                                !isOnline -> "Offline: invio non disponibile"
                                state.daInviareCount > 0 -> "Invia ad API (${state.daInviareCount})"
                                else -> "Nessun dato da trasferire"
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { mostraConfermaPulizia = true },
                        enabled = perizieInviate > 0 && !state.trasferimento.puliziaInCorso,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            when {
                                state.trasferimento.puliziaInCorso -> "Cancellazione dati..."
                                perizieInviate > 0 -> "Cancella perizie inviate ($perizieInviate)"
                                else -> "Nessuna perizia inviata da cancellare"
                            },
                            color = if (perizieInviate > 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }

    if (mostraConfermaPulizia) {
        ConfirmDialog(
            title = "Cancellare i dati inviati?",
            text = "Verranno cancellate dal telefono $perizieInviate perizie gia' inviate e $fotoInviate foto collegate. Uso temporaneo per fase test.",
            confirmText = "Cancella inviati",
            onConfirm = {
                mostraConfermaPulizia = false
                onPulisciFotoInviateClick()
            },
            onDismiss = { mostraConfermaPulizia = false }
        )
    }
}

@Composable
private fun TransferStatusRow(
    label: String,
    value: String,
    status: SyncStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            StatusChip(status = status)
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun NuovaDekraPeriziaDialog(
    commessa: DekraCommessaSeed,
    targa: String,
    errore: String?,
    onTargaChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova targa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = commessa.descrizione,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = targa,
                    onValueChange = onTargaChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Targa") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onConfirm() })
                )
                errore?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Crea")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun OnlineStatusCard(isOnline: Boolean) {
    val background = if (isOnline) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
    }
    val foreground = if (isOnline) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val titolo = if (isOnline) "Online" else "Offline"
    val dettaglio = if (isOnline) {
        "Invio dati disponibile."
    } else {
        "Puoi lavorare e fotografare; l'invio email e' disattivato."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titolo,
                style = MaterialTheme.typography.titleMedium,
                color = foreground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dettaglio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
private fun SummarySection(total: Int, daInviare: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryTile(
            label = "Totale locale",
            value = total.toString(),
            modifier = Modifier.weight(1f)
        )
        SummaryTile(
            label = "Da inviare",
            value = daInviare.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PeriziaFormCard(
    state: PerizieUiState,
    onTargaChange: (String) -> Unit,
    onTelaioChange: (String) -> Unit,
    onModelloChange: (String) -> Unit,
    onScattaFotoClick: () -> Unit,
    onScegliFotoClick: () -> Unit,
    onRimuoviFotoClick: (String) -> Unit,
    onSalvaClick: () -> Unit,
    onAnnullaModificaClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle(
                if (state.form.isModifica) "Modifica perizia" else "Nuova perizia"
            )

            OutlinedTextField(
                value = state.form.targa,
                onValueChange = onTargaChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Targa") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                )
            )


            FotoSection(
                fotoPaths = state.form.fotoPaths,
                onScattaFotoClick = onScattaFotoClick,
                onScegliFotoClick = onScegliFotoClick,
                onRimuoviFotoClick = onRimuoviFotoClick
            )

            state.form.errore?.let { errore ->
                Text(
                    text = errore,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onSalvaClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (state.form.isModifica) "Aggiorna perizia" else "Salva perizia")
            }

            if (state.form.isModifica) {
                OutlinedButton(
                    onClick = onAnnullaModificaClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Annulla modifica")
                }
            }
        }
    }
}

@Composable
private fun FotoSection(
    fotoPaths: List<String>,
    onScattaFotoClick: () -> Unit,
    onScegliFotoClick: () -> Unit,
    onRimuoviFotoClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (fotoPaths.isEmpty()) {
            EmptyPhotoPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        } else {
            fotoPaths.forEachIndexed { index, path ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Foto ${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    PhotoPreview(
                        fotoPath = path,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    OutlinedButton(
                        onClick = { onRimuoviFotoClick(path) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Rimuovi foto ${index + 1}")
                    }
                }
            }
        }

        Button(
            onClick = onScattaFotoClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Scatta foto")
        }

        OutlinedButton(
            onClick = onScegliFotoClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Scegli foto esistente")
        }

    }
}

@Composable
private fun PhotoPreview(
    fotoPath: String?,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(fotoPath) {
        fotoPath?.let { path ->
            decodePreviewImage(path)
        }
    }

    if (imageBitmap == null) {
        EmptyPhotoPreview(modifier = modifier)
    } else {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Foto perizia",
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun EmptyPhotoPreview(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Nessuna foto associata")
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nessuna perizia salvata.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun PeriziaListItem(
    periziaConFoto: PeriziaConFoto,
    onClick: () -> Unit
) {
    val perizia = periziaConFoto.perizia
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = perizia.targa,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (perizia.modello.isNotBlank()) {
                        Text(
                            text = perizia.modello,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                StatusChip(status = perizia.syncStatus)
            }

            if (perizia.telaio.isNotBlank()) {
                Text(
                    text = "Telaio: ${perizia.telaio}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Data perizia: ${formatter.format(Instant.ofEpochMilli(perizia.dataPerizia))}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            if (periziaConFoto.foto.isNotEmpty()) {
                Text(
                    text = "Foto associate: ${periziaConFoto.foto.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: SyncStatus) {
    val color = when (status) {
        SyncStatus.DA_INVIARE -> Color(0xFFFFE7B3)
        SyncStatus.INVIATO -> Color(0xFFD8F3DC)
        SyncStatus.ERRORE -> Color(0xFFFFDAD6)
    }
    val textColor = when (status) {
        SyncStatus.DA_INVIARE -> Color(0xFF684800)
        SyncStatus.INVIATO -> Color(0xFF0B5D1E)
        SyncStatus.ERRORE -> Color(0xFF8C1D18)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private val SyncStatus.label: String
    get() = when (this) {
        SyncStatus.DA_INVIARE -> "Da inviare"
        SyncStatus.INVIATO -> "Inviato"
        SyncStatus.ERRORE -> "Errore"
    }

@Composable
private fun rememberAppVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        }.getOrDefault("")
    }
}
@Composable
private fun rememberOnlineStatus(): Boolean {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(isDeviceOnline(context)) }

    LaunchedEffect(context) {
        while (true) {
            isOnline = isDeviceOnline(context)
            delay(3_000)
        }
    }

    return isOnline
}

private fun isDeviceOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
private fun createPhotoFile(context: Context): File {
    val directory = File(context.filesDir, "perizia_foto").apply {
        mkdirs()
    }
    return File(directory, "perizia_${System.currentTimeMillis()}.jpg")
}

private fun createPhotoUri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun inviaPacchettoTrasferimentoEmail(context: Context, path: String) {
    val file = File(path)
    if (!file.exists()) return

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        if (DESTINATARIO_TRASFERIMENTO.isNotBlank()) {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(DESTINATARIO_TRASFERIMENTO))
        }
        putExtra(Intent.EXTRA_SUBJECT, "Pacchetto dati DEKRA")
        putExtra(Intent.EXTRA_TEXT, "In allegato il pacchetto dati DEKRA esportato dall'app RilevamentoDati.")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Invia pacchetto dati")) }
}

private fun copyPhotoToAppStorage(context: Context, uri: Uri): String? {
    val file = createPhotoFile(context)
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        ottimizzaFotoPerInvio(file)
        file.absolutePath
    }.getOrNull()
}

private fun ottimizzaFotoPerInvio(file: File) {
    runCatching {
        if (!file.exists()) return@runCatching
        var bitmap = decodeBitmapPerInvio(file, FOTO_MAX_DIMENSION) ?: return@runCatching
        bitmap = ruotaBitmapDaExif(bitmap, file.absolutePath)
        var maxDimension = FOTO_MAX_DIMENSION
        var migliore = comprimiJpeg(bitmap, 82)

        while (true) {
            for (quality in 82 downTo 50 step 4) {
                val candidate = comprimiJpeg(bitmap, quality)
                migliore = candidate
                if (candidate.size <= FOTO_TARGET_MAX_BYTES) {
                    file.writeBytes(candidate)
                    bitmap.recycle()
                    return@runCatching
                }
            }

            val latoLungo = maxOf(bitmap.width, bitmap.height)
            if (latoLungo <= FOTO_MIN_DIMENSION) {
                file.writeBytes(migliore)
                bitmap.recycle()
                return@runCatching
            }

            maxDimension = (maxDimension * 0.85f).toInt().coerceAtLeast(FOTO_MIN_DIMENSION)
            val ridotta = scalaBitmap(bitmap, maxDimension)
            if (ridotta !== bitmap) {
                bitmap.recycle()
                bitmap = ridotta
            } else {
                file.writeBytes(migliore)
                bitmap.recycle()
                return@runCatching
            }
        }
    }
}

private fun decodeBitmapPerInvio(file: File, maxDimension: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (
        bounds.outWidth / sampleSize > maxDimension * 2 ||
        bounds.outHeight / sampleSize > maxDimension * 2
    ) {
        sampleSize *= 2
    }

    val decoded = BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply { inSampleSize = sampleSize }
    ) ?: return null
    return scalaBitmap(decoded, maxDimension).also { scaled ->
        if (scaled !== decoded) decoded.recycle()
    }
}

private fun scalaBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val latoLungo = maxOf(bitmap.width, bitmap.height)
    if (latoLungo <= maxDimension) return bitmap
    val scale = maxDimension.toFloat() / latoLungo.toFloat()
    val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
    val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

private fun ruotaBitmapDaExif(bitmap: Bitmap, path: String): Bitmap {
    val orientation = runCatching {
        ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    val degrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (degrees == 0f) return bitmap
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
        bitmap.recycle()
    }
}

private fun comprimiJpeg(bitmap: Bitmap, quality: Int): ByteArray {
    return ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        output.toByteArray()
    }
}

private fun decodePreviewImage(path: String) = runCatching {
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(path, bounds)

    var sampleSize = 1
    val maxPreviewSize = 1200
    while (
        bounds.outWidth / sampleSize > maxPreviewSize ||
        bounds.outHeight / sampleSize > maxPreviewSize
    ) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    }
    BitmapFactory.decodeFile(path, options)?.asImageBitmap()
}.getOrNull()
