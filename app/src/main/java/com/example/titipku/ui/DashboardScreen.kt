package com.example.titipku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipku.UserEntity

@Composable
fun DashboardScreen(user: UserEntity?) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(text = "Halo, ${user?.namaToko ?: "Mitra"}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCard("Terjual", "452", Color(0xFF1E88E5))
                StatCard("Keuntungan", "Rp 12,4jt", Color(0xFF2E7D32))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            Text("Barang Terlaris", fontWeight = FontWeight.Bold)
            // Sekarang memanggil fungsi dari file BarangItem.kt
            BarangItem("Tahu Ngacir Crispy", 150, "15.000")
            BarangItem("Sambal Cocol", 80, "5.000")
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.White)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}