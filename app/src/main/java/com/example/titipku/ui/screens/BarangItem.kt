package com.example.titipku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BarangItem(namaBarang: String, jumlah: Int, harga: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = namaBarang, style = MaterialTheme.typography.titleMedium)
                Text(text = "Stok: $jumlah", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "Rp $harga", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}