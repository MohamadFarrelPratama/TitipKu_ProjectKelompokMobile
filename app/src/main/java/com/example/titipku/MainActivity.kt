package com.example.titipku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.titipku.ui.MainNavigationApp

// PERHATIAN: Kamu mungkin perlu menekan Alt+Enter (Import) untuk 2 baris ini
// jika file AppDatabase dan MainNavigationApp ada di folder (package) lain.
// import com.example.titipku.database.AppDatabase
// import com.example.titipku.ui.MainNavigationApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi Database Room
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.titipKuDao()

        // 2. Set Tampilan UI (Compose)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF7F9FC)
                ) {
                    // 3. Panggil navigasi atau layar utama kamu
                    MainNavigationApp(dao)
                }
            }
        }
    }
}