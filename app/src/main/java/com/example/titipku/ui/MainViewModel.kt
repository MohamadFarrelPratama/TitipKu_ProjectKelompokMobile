package com.example.titipku.ui

import androidx.lifecycle.ViewModel
import com.example.titipku.TitipKuDao
import com.example.titipku.BarangEntity // Pastikan sesuaikan dengan nama entity barangmu

class MainViewModel(private val dao: TitipKuDao) : ViewModel() {
    // Mengambil data dari Room Database sebagai Flow
    val allBarang = dao.getAllBarang()

    // Fungsi untuk menghitung total penjualan bisa ditambah di sini nanti
}