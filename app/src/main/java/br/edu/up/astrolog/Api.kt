package br.edu.up.astrolog

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API pública Open-Meteo (sem chave): condições do céu em tempo real e geocodificação
data class ClimaResposta(val current: ClimaAtual)
data class ClimaAtual(val cloud_cover: Int, val temperature_2m: Double)

data class GeoResposta(val results: List<GeoLocal>?)
data class GeoLocal(val name: String, val latitude: Double, val longitude: Double, val admin1: String?)

interface ClimaApi {
    @GET("v1/forecast")
    suspend fun clima(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "cloud_cover,temperature_2m"
    ): ClimaResposta
}

interface GeoApi {
    @GET("v1/search")
    suspend fun buscarCidade(
        @Query("name") nome: String,
        @Query("count") count: Int = 1,
        @Query("language") lang: String = "pt"
    ): GeoResposta
}

object RetrofitClient {
    val api: ClimaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
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
