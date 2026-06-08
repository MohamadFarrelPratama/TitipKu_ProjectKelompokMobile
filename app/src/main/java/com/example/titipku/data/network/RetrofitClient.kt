package com.example.titipku.com.example.titipku.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Sesuaikan dengan model data GistApiResponse milikmu
data class GistApiResponse(
    val daftar_toko: List<TokoGist>,
    val daftar_barang: List<BarangGist>
)
data class TokoGist(val nama: String, val jmlBarang: Int, val status: String, val url_gambar: String)
data class BarangGist(val nama: String, val harga: Int, val stok: Int, val status: String, val url_gambar: String)

interface GistApiService {
    @GET("MohamadFarrelPratama/7aa75afecaa4b411e2c818b3dceca20d/raw/titipku_api.json")
    suspend fun getTitipKuData(): GistApiResponse
}

object RetrofitClient {
    val instance: GistApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://gist.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GistApiService::class.java)
    }
}