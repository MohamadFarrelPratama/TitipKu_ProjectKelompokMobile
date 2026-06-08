package com.example.titipku.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipku.TitipKuDao
import com.example.titipku.UserEntity
import com.example.titipku.ui.screens.DashboardScreen
import com.example.titipku.ui.screens.LoginScreen

@Composable
fun MainNavigationApp(dao: TitipKuDao) {
    val currentScreen = remember { mutableStateOf("LOGIN") }
    val currentUserActive = remember { mutableStateOf<UserEntity?>(null) }

    val listBarangState by dao.getAllBarang().collectAsState(initial = emptyList())
    val listTokoState by dao.getAllToko().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var totalTerjualKumulatif by remember { mutableIntStateOf(452) }
    var totalKeuntunganKumulatif by remember { mutableIntStateOf(12450000) }

    when (currentScreen.value) {
        "LOGIN" -> LoginScreen(
            dao = dao,
            onLoginSuccess = { user: UserEntity ->
                currentUserActive.value = user
                currentScreen.value = "DASHBOARD"
            },
            onRegisterClick = { currentScreen.value = "REGISTRASI" }
        )
        "REGISTRASI" -> {
            Text("Halaman Registrasi (Sedang dipersiapkan)")
        }
        "DASHBOARD" -> MainLayout(currentTab = 0, onTabSelected = { screen -> currentScreen.value = screen }) {
            DashboardScreen(
                currentUserActive.value)
        }
        "BARANG" -> MainLayout(currentTab = 1, onTabSelected = { screen -> currentScreen.value = screen }) {
            Text("Halaman Daftar Barang")
        }
        "TOKO" -> MainLayout(currentTab = 2, onTabSelected = { screen -> currentScreen.value = screen }) {
            Text("Halaman Mitra Toko")
        }
        "LAPORAN" -> MainLayout(currentTab = 3, onTabSelected = { screen -> currentScreen.value = screen }) {
            Text("Halaman Laporan")
        }
        "PENGATURAN" -> MainLayout(currentTab = 4, onTabSelected = { screen -> currentScreen.value = screen }) {
            Text("Halaman Pengaturan")
        }
    }
}

@Composable
fun MainLayout(currentTab: Int, onTabSelected: (String) -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                val items = listOf("Dashboard" to "DASHBOARD", "Barang" to "BARANG", "Toko" to "TOKO", "Laporan" to "LAPORAN", "Pengaturan" to "PENGATURAN")
                val icons = listOf(Icons.Default.Dashboard, Icons.AutoMirrored.Filled.ListAlt, Icons.Default.Storefront, Icons.Default.Assessment, Icons.Default.Settings)

                items.forEachIndexed { index, pair ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { onTabSelected(pair.second) },
                        icon = { Icon(icons[index], contentDescription = pair.first) },
                        label = { Text(pair.first, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E88E5),
                            selectedTextColor = Color(0xFF1E88E5),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE3F2FD)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}