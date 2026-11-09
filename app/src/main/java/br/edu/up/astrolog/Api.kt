package br.edu.up.astrolog

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// =========================================================================
// REQUISITO: CONSUMO DE API (Retrofit + Gson)
// Utilizamos a API pública Open-Meteo (não precisa de chave de API).
// 1. ClimaApi: busca cobertura de nuvens (%) e temperatura (°C) pelas coordenadas.
// 2. GeoApi: busca latitude e longitude pelo nome da cidade (Geocodificação).
// =========================================================================

// Modelos de dados (Data Classes) que recebem o JSON da API de Clima
data class ClimaResposta(val current: ClimaAtual)
data class ClimaAtual(val cloud_cover: Int, val temperature_2m: Double)

// Modelos de dados que recebem o JSON da API de Geocodificação (busca por cidade)
data class GeoResposta(val results: List<GeoLocal>?)
data class GeoLocal(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String? // Estado / Região (ex: Paraná)
)

// Interface do Retrofit: define os endpoints HTTP GET
interface ClimaApi {
    // Exemplo de chamada: https://api.open-meteo.com/v1/forecast?latitude=-25.42&longitude=-49.27&current=cloud_cover,temperature_2m
    @GET("v1/forecast")
    suspend fun clima(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "cloud_cover,temperature_2m"
    ): ClimaResposta
}

interface GeoApi {
    // Exemplo de chamada: https://geocoding-api.open-meteo.com/v1/search?name=Curitiba&count=1&language=pt
    @GET("v1/search")
    suspend fun buscarCidade(
        @Query("name") nome: String,
        @Query("count") count: Int = 1,
        @Query("language") lang: String = "pt"
    ): GeoResposta
}

// Singleton (object) que cria e fornece as instâncias do Retrofit sob demanda (by lazy)
object RetrofitClient {
    val api: ClimaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create()) // Converte JSON para Data Class
            .build()
            .create(ClimaApi::class.java)
    }

    val geoApi: GeoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoApi::class.java)
    }
}
