package br.edu.up.astrolog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// =========================================================================
// ARQUITETURA MVVM: ViewModel
// O ViewModel sobrevive à rotação de tela, guarda o estado da UI (StateFlow)
// e executa as operações de banco (Room) e de rede (Retrofit) em Coroutines
// usando o viewModelScope.
// =========================================================================

class AstroViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val localDao = db.localDao()
    private val obsDao = db.observacaoDao()

    // Converte o Flow do Room em StateFlow para a interface Compose observar em tempo real
    val locais = localDao.todos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val observacoes = obsDao.todas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mapa que guarda o texto de condição do céu por ID de local (resultado da API)
    private val _clima = MutableStateFlow<Map<Long, String>>(emptyMap())
    val clima: StateFlow<Map<Long, String>> = _clima

    // ---------- CRUD Tabela 1: Local ----------
    // Se id == 0L é um registro novo (Create / Insert); caso contrário, já existe (Update)
    fun salvarLocal(l: Local) = viewModelScope.launch {
        if (l.id == 0L) localDao.inserir(l) else localDao.atualizar(l)
    }
    fun excluirLocal(l: Local) = viewModelScope.launch { localDao.excluir(l) }

    // ---------- CRUD Tabela 2: Observacao ----------
    fun salvarObservacao(o: Observacao) = viewModelScope.launch {
        if (o.id == 0L) obsDao.inserir(o) else obsDao.atualizar(o)
    }
    fun excluirObservacao(o: Observacao) = viewModelScope.launch { obsDao.excluir(o) }

    // ---------- Consumo de API 1: Condições do céu (Open-Meteo Forecast) ----------
    fun consultarClima(l: Local, aoConcluir: ((String) -> Unit)? = null) = viewModelScope.launch {
        _clima.update { it + (l.id to "Consultando...") }
        val texto = try {
            val c = RetrofitClient.api.clima(l.latitude, l.longitude).current
            val veredito = when {
                c.cloud_cover < 30 -> "céu ótimo para observar! 🌌"
                c.cloud_cover < 70 -> "céu parcialmente encoberto"
                else -> "muito nublado, melhor deixar para outra noite"
            }
            "☁ ${c.cloud_cover}% de nuvens · ${c.temperature_2m}°C · $veredito"
        } catch (e: Exception) {
            "Não foi possível consultar (verifique a internet)"
        }
        _clima.update { it + (l.id to texto) }
        aoConcluir?.invoke(texto)
    }

    // ---------- Consumo de API 2: Busca de coordenadas pelo nome da cidade ----------
    fun buscarCoordenadas(cidade: String, aoConcluir: (GeoLocal?) -> Unit) = viewModelScope.launch {
        val resultado = try {
            RetrofitClient.geoApi.buscarCidade(cidade.trim()).results?.firstOrNull()
        } catch (e: Exception) {
            null
        }
        aoConcluir(resultado)
    }
}
