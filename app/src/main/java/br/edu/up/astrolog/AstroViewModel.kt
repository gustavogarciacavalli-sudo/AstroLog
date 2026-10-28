package br.edu.up.astrolog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AstroViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val localDao = db.localDao()
    private val obsDao = db.observacaoDao()

    val locais = localDao.todos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val observacoes = obsDao.todas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // texto de condição do céu por id de local (vem da API)
    private val _clima = MutableStateFlow<Map<Long, String>>(emptyMap())
    val clima: StateFlow<Map<Long, String>> = _clima

    fun salvarLocal(l: Local) = viewModelScope.launch {
        if (l.id == 0L) localDao.inserir(l) else localDao.atualizar(l)
    }
    fun excluirLocal(l: Local) = viewModelScope.launch { localDao.excluir(l) }

    fun salvarObservacao(o: Observacao) = viewModelScope.launch {
        if (o.id == 0L) obsDao.inserir(o) else obsDao.atualizar(o)
    }
    fun excluirObservacao(o: Observacao) = viewModelScope.launch { obsDao.excluir(o) }

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

    fun buscarCoordenadas(cidade: String, aoConcluir: (GeoLocal?) -> Unit) = viewModelScope.launch {
        val resultado = try {
            RetrofitClient.geoApi.buscarCidade(cidade.trim()).results?.firstOrNull()
        } catch (e: Exception) {
            null
        }
        aoConcluir(resultado)
    }
}
