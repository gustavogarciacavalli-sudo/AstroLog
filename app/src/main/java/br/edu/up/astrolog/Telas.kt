package br.edu.up.astrolog

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroApp(vm: AstroViewModel = viewModel()) {
    var aba by rememberSaveable { mutableIntStateOf(0) }
    val locais by vm.locais.collectAsState()
    val obs by vm.observacoes.collectAsState()
    val clima by vm.clima.collectAsState()
    val ctx = LocalContext.current

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("AstroLog 🔭") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = aba == 0, onClick = { aba = 0 },
                    icon = { Icon(Icons.Default.Place, null) }, label = { Text("Locais (${locais.size})") })
                NavigationBarItem(selected = aba == 1, onClick = { aba = 1 },
                    icon = { Icon(Icons.Default.Star, null) }, label = { Text("Observações (${obs.size})") })
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            if (aba == 0) {
                LocaisTela(
                    locais = locais,
                    clima = clima,
                    onConsultar = { vm.consultarClima(it) },
                    onBuscarCoordenadas = vm::buscarCoordenadas,
                    onSalvar = vm::salvarLocal,
                    onExcluir = vm::excluirLocal
                )
            } else {
                ObservacoesTela(
                    obs = obs,
                    locais = locais,
                    clima = clima,
                    onConsultarClima = vm::consultarClima,
                    onSalvar = vm::salvarObservacao,
                    onExcluir = vm::excluirObservacao
                ) { o ->
                    compartilhar(ctx, o, locais.firstOrNull { it.id == o.localId }?.nome ?: "-")
                }
            }
        }
    }
}

// ---------- Compartilhamento com outros apps instalados (WhatsApp, e-mail, etc.) ----------
fun compartilhar(ctx: Context, o: Observacao, local: String) {
    val textoClima = if (o.condicaoCeu.isNotBlank()) "\n${o.condicaoCeu}" else ""
    val textoNotas = if (o.notas.isNotBlank()) "\n📝 ${o.notas}" else ""
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Observação: ${o.objeto}")
        putExtra(
            Intent.EXTRA_TEXT,
            "🔭 ${o.objeto}\n📍 $local\n📅 ${o.data}\n⭐ ${o.nota}/5$textoClima$textoNotas"
        )
    }
    ctx.startActivity(Intent.createChooser(i, "Compartilhar observação"))
}

// ====================== LOCAIS ======================
@Composable
fun LocaisTela(
    locais: List<Local>,
    clima: Map<Long, String>,
    onConsultar: (Local) -> Unit,
    onBuscarCoordenadas: (String, (GeoLocal?) -> Unit) -> Unit,
    onSalvar: (Local) -> Unit,
    onExcluir: (Local) -> Unit
) {
    // null = fechado, -1 = novo, outro = id em edição (sobrevive à rotação de tela)
    var editId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    Box(Modifier.fillMaxSize()) {
        if (locais.isEmpty()) {
            Text(
                "Nenhum local ainda. Toque em + para cadastrar o seu ponto de observação.",
                Modifier.align(Alignment.Center).padding(24.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // bottom = 88.dp evita sobreposição do FloatingActionButton sobre o último item
            contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(locais, key = { it.id }) { l ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(l.nome, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Lat ${l.latitude} · Lon ${l.longitude}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        clima[l.id]?.let {
                            Text(
                                it,
                                modifier = Modifier.padding(top = 6.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { onConsultar(l) }) { Text("Condições do céu (API)") }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { editId = l.id }) { Icon(Icons.Default.Edit, "Editar") }
                            IconButton(onClick = { deleteId = l.id }) { Icon(Icons.Default.Delete, "Excluir") }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { editId = -1L },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Novo local")
        }
    }

    editId?.let { id ->
        val inicial = locais.firstOrNull { it.id == id }
        if (id == -1L || inicial != null) {
            LocalDialog(
                inicial = inicial,
                onBuscarCoordenadas = onBuscarCoordenadas,
                onDismiss = { editId = null }
            ) {
                onSalvar(it)
                editId = null
            }
        }
    }

    deleteId?.let { id ->
        val alvo = locais.firstOrNull { it.id == id }
        if (alvo != null) {
            AlertDialog(
                onDismissRequest = { deleteId = null },
                title = { Text("Excluir local?") },
                text = { Text("Deseja excluir \"${alvo.nome}\"? Todas as observações vinculadas a este local também serão apagadas.") },
                confirmButton = {
                    TextButton(onClick = { onExcluir(alvo); deleteId = null }) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteId = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun LocalDialog(
    inicial: Local?,
    onBuscarCoordenadas: (String, (GeoLocal?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSalvar: (Local) -> Unit
) {
    var nome by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.nome ?: "") }
    var lat by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.latitude?.toString() ?: "") }
    var lon by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.longitude?.toString() ?: "") }
    var msgStatus by rememberSaveable(inicial?.id) { mutableStateOf("") }
    val num = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (inicial == null) "Novo local" else "Editar local") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it; msgStatus = "" },
                    label = { Text("Nome ou Cidade (ex: Curitiba)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        if (nome.isBlank()) {
                            msgStatus = "Digite um nome de cidade para buscar."
                        } else {
                            msgStatus = "Buscando coordenadas na API..."
                            onBuscarCoordenadas(nome) { geo ->
                                if (geo != null) {
                                    lat = geo.latitude.toString()
                                    lon = geo.longitude.toString()
                                    if (nome.isBlank()) nome = geo.name
                                    msgStatus = "Coordenadas preenchidas (${geo.name}${geo.admin1?.let { ", $it" } ?: ""})"
                                } else {
                                    msgStatus = "Cidade não encontrada ou sem internet. Digite manualmente."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Buscar coordenadas pelo nome (API)")
                }
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it; msgStatus = "" },
                    label = { Text("Latitude (-90 a 90, ex: -25.42)") },
                    keyboardOptions = num,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lon,
                    onValueChange = { lon = it; msgStatus = "" },
                    label = { Text("Longitude (-180 a 180, ex: -49.27)") },
                    keyboardOptions = num,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (msgStatus.isNotBlank()) {
                    Text(
                        msgStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val la = lat.replace(',', '.').toDoubleOrNull()
                val lo = lon.replace(',', '.').toDoubleOrNull()
                when {
                    nome.isBlank() -> msgStatus = "Preencha o nome do local."
                    la == null || la !in -90.0..90.0 -> msgStatus = "Latitude inválida (use valores entre -90 e 90)."
                    lo == null || lo !in -180.0..180.0 -> msgStatus = "Longitude inválida (use valores entre -180 e 180)."
                    else -> onSalvar(
                        (inicial ?: Local(nome = "", latitude = 0.0, longitude = 0.0))
                            .copy(nome = nome.trim(), latitude = la, longitude = lo)
                    )
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ====================== OBSERVAÇÕES ======================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservacoesTela(
    obs: List<Observacao>,
    locais: List<Local>,
    clima: Map<Long, String>,
    onConsultarClima: (Local, ((String) -> Unit)?) -> Unit,
    onSalvar: (Observacao) -> Unit,
    onExcluir: (Observacao) -> Unit,
    onCompartilhar: (Observacao) -> Unit
) {
    var editId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var filtroLocalId by rememberSaveable { mutableStateOf<Long?>(null) }

    val obsFiltradas = remember(obs, filtroLocalId) {
        if (filtroLocalId == null) obs else obs.filter { it.localId == filtroLocalId }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (locais.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filtroLocalId == null,
                        onClick = { filtroLocalId = null },
                        label = { Text("Todos") }
                    )
                    locais.forEach { l ->
                        FilterChip(
                            selected = filtroLocalId == l.id,
                            onClick = { filtroLocalId = l.id },
                            label = { Text(l.nome) }
                        )
                    }
                }
            }

            Box(Modifier.fillMaxSize()) {
                if (locais.isEmpty()) {
                    Text(
                        "Cadastre primeiro um local na aba Locais.",
                        Modifier.align(Alignment.Center).padding(24.dp)
                    )
                } else if (obsFiltradas.isEmpty()) {
                    Text(
                        "Nenhuma observação registrada. Toque em + para começar.",
                        Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    // bottom = 88.dp evita sobreposição do FloatingActionButton sobre o último item
                    contentPadding = PaddingValues(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(obsFiltradas, key = { it.id }) { o ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(o.objeto, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${o.data} · ${locais.firstOrNull { it.id == o.localId }?.nome ?: "-"} · " +
                                            "⭐".repeat(o.nota),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (o.condicaoCeu.isNotBlank()) {
                                    Text(
                                        o.condicaoCeu,
                                        modifier = Modifier.padding(top = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (o.notas.isNotBlank()) {
                                    Text(o.notas, Modifier.padding(top = 6.dp))
                                }
                                Row {
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { onCompartilhar(o) }) {
                                        Icon(Icons.Default.Share, "Compartilhar")
                                    }
                                    IconButton(onClick = { editId = o.id }) {
                                        Icon(Icons.Default.Edit, "Editar")
                                    }
                                    IconButton(onClick = { deleteId = o.id }) {
                                        Icon(Icons.Default.Delete, "Excluir")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (locais.isNotEmpty()) {
            FloatingActionButton(
                onClick = { editId = -1L },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Add, "Nova observação")
            }
        }
    }

    editId?.let { id ->
        val inicial = obs.firstOrNull { it.id == id }
        if (locais.isNotEmpty() && (id == -1L || inicial != null)) {
            ObservacaoDialog(
                inicial = inicial,
                locais = locais,
                clima = clima,
                onConsultarClima = onConsultarClima,
                onDismiss = { editId = null }
            ) {
                onSalvar(it)
                editId = null
            }
        }
    }

    deleteId?.let { id ->
        val alvo = obs.firstOrNull { it.id == id }
        if (alvo != null) {
            AlertDialog(
                onDismissRequest = { deleteId = null },
                title = { Text("Excluir observação?") },
                text = { Text("Deseja excluir a observação de \"${alvo.objeto}\"?") },
                confirmButton = {
                    TextButton(onClick = { onExcluir(alvo); deleteId = null }) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteId = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservacaoDialog(
    inicial: Observacao?,
    locais: List<Local>,
    clima: Map<Long, String>,
    onConsultarClima: (Local, ((String) -> Unit)?) -> Unit,
    onDismiss: () -> Unit,
    onSalvar: (Observacao) -> Unit
) {
    val primeiroLocalId = locais.firstOrNull()?.id ?: 0L
    var objeto by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.objeto ?: "") }
    var data by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.data ?: LocalDate.now().toString()) }
    var notas by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.notas ?: "") }
    var nota by rememberSaveable(inicial?.id) { mutableFloatStateOf((inicial?.nota ?: 3).toFloat()) }
    var localId by rememberSaveable(inicial?.id) { mutableLongStateOf(inicial?.localId ?: primeiroLocalId) }
    var condicaoCeu by rememberSaveable(inicial?.id) {
        mutableStateOf(inicial?.condicaoCeu ?: clima[inicial?.localId ?: primeiroLocalId] ?: "")
    }
    var erro by rememberSaveable(inicial?.id) { mutableStateOf("") }

    val localSelecionado = locais.firstOrNull { it.id == localId } ?: locais.firstOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (inicial == null) "Nova observação" else "Editar observação") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = objeto,
                    onValueChange = { objeto = it; erro = "" },
                    label = { Text("Objeto celeste (ex: Saturno)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it; erro = "" },
                    label = { Text("Data (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Local:")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    locais.forEach { l ->
                        FilterChip(
                            selected = localSelecionado?.id == l.id,
                            onClick = {
                                localId = l.id
                                clima[l.id]?.let { if (condicaoCeu.isBlank()) condicaoCeu = it }
                            },
                            label = { Text(l.nome) }
                        )
                    }
                }
                OutlinedTextField(
                    value = condicaoCeu,
                    onValueChange = { condicaoCeu = it },
                    label = { Text("Condições do céu (API)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        localSelecionado?.let { loc ->
                            condicaoCeu = "Consultando API..."
                            onConsultarClima(loc) { res -> condicaoCeu = res }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Consultar clima atual na API")
                }
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Anotações") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Qualidade da observação: ${nota.roundToInt()}/5")
                Slider(nota, { nota = it }, valueRange = 1f..5f, steps = 3)
                if (erro.isNotBlank()) {
                    Text(
                        erro,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val idFinal = localSelecionado?.id ?: primeiroLocalId
                when {
                    objeto.isBlank() -> erro = "Informe o objeto observado."
                    data.isBlank() -> erro = "Informe a data da observação."
                    else -> onSalvar(
                        (inicial ?: Observacao(
                            localId = idFinal,
                            objeto = "",
                            data = "",
                            notas = "",
                            nota = 3
                        )).copy(
                            localId = idFinal,
                            objeto = objeto.trim(),
                            data = data.trim(),
                            notas = notas.trim(),
                            nota = nota.roundToInt(),
                            condicaoCeu = condicaoCeu.trim()
                        )
                    )
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
