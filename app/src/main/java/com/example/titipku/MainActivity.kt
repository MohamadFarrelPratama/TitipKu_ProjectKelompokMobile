package com.example.titipku

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// ==========================================
// CONFIG NETWORK RETROFIT (API EXTERNAL)
// ==========================================
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.titipKuDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (dao.getBarangCount() == 0 && dao.getTokoCount() == 0) {
                    val remoteData = RetrofitClient.instance.getTitipKuData()

                    val initialToko = remoteData.daftar_toko.map {
                        TokoEntity(nama = it.nama, jmlBarang = it.jmlBarang, status = it.status, urlGambar = it.url_gambar)
                    }
                    val initialBarang = remoteData.daftar_barang.map {
                        BarangEntity(nama = it.nama, harga = it.harga, stok = it.stok, status = it.status, urlGambar = it.url_gambar)
                    }

                    dao.insertInitialToko(initialToko)
                    dao.insertInitialBarang(initialBarang)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF7F9FC)
                ) {
                    MainNavigationApp(dao)
                }
            }
        }
    }
}

@Composable
fun MainNavigationApp(dao: TitipKuDao) {
    // Menggunakan state object secara eksplisit untuk menghilangkan warning "Assigned value is never read"
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
            onLoginSuccess = { user ->
                currentUserActive.value = user
                currentScreen.value = "DASHBOARD"
            },
            onRegisterClick = { currentScreen.value = "REGISTRASI" }
        )
        "REGISTRASI" -> RegistrasiScreen(
            dao = dao,
            onRegisterSuccess = { currentScreen.value = "LOGIN" },
            onBackToLogin = { currentScreen.value = "LOGIN" }
        )
        "DASHBOARD" -> MainLayout(currentTab = 0, onTabSelected = { screen -> currentScreen.value = screen }) {
            DashboardView(
                namaToko = currentUserActive.value?.namaToko ?: "Wijaya Suksma",
                totalBarang = listBarangState.size,
                totalTerjual = totalTerjualKumulatif,
                totalKeuntungan = totalKeuntunganKumulatif,
                onNavigate = { screen -> currentScreen.value = screen }
            )
        }
        "BARANG" -> MainLayout(currentTab = 1, onTabSelected = { screen -> currentScreen.value = screen }) {
            BarangView(listBarangState, onAddBarangClick = { currentScreen.value = "ADD_DATA" })
        }
        "TOKO" -> MainLayout(currentTab = 2, onTabSelected = { screen -> currentScreen.value = screen }) {
            TokoView(listTokoState, onAddTokoClick = { currentScreen.value = "ADD_TOKO" })
        }
        "LAPORAN" -> MainLayout(currentTab = 3, onTabSelected = { screen -> currentScreen.value = screen }) {
            LaporanView(totalKeuntungan = totalKeuntunganKumulatif, produkTerlaris = "Tahu Crispy ($totalTerjualKumulatif terjual)")
        }
        "PENGATURAN" -> MainLayout(currentTab = 4, onTabSelected = { screen -> currentScreen.value = screen }) {
            PengaturanView(
                username = currentUserActive.value?.username ?: "User",
                namaToko = currentUserActive.value?.namaToko ?: "Mitra",
                onProfileClick = { currentScreen.value = "PROFILE" },
                onLogout = { currentScreen.value = "LOGIN" }
            )
        }
        "TRANSACTION_INPUT" -> TransactionInputScreen(
            listToko = listTokoState,
            listBarang = listBarangState,
            onBack = { currentScreen.value = "DASHBOARD" },
            onSave = { _, barang, qty ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val stokSisa = (barang.stok - qty).coerceAtLeast(0)
                        val statusBaru = if (stokSisa <= 5) "Low Stock" else "Tersedia"
                        dao.insertBarang(barang.copy(stok = stokSisa, status = statusBaru))
                    }
                    totalTerjualKumulatif += qty
                    totalKeuntunganKumulatif += (barang.harga * qty)
                    currentScreen.value = "DASHBOARD"
                }
            }
        )
        "ADD_DATA" -> AddDataScreen(
            onBack = { currentScreen.value = "BARANG" },
            onSave = { nama, harga, stok ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        dao.insertBarang(BarangEntity(nama = nama, harga = harga, stok = stok, status = if(stok <= 5) "Low Stock" else "Tersedia", urlGambar = ""))
                    }
                    currentScreen.value = "BARANG"
                }
            }
        )
        "ADD_TOKO" -> AddTokoScreen(
            onBack = { currentScreen.value = "TOKO" },
            onSave = { namaToko ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        dao.insertToko(TokoEntity(nama = namaToko, jmlBarang = 0, status = "Aktif", urlGambar = ""))
                    }
                    currentScreen.value = "TOKO"
                }
            }
        )
        "PROFILE" -> ProfileScreen(
            username = currentUserActive.value?.username ?: "ilham",
            namaToko = currentUserActive.value?.namaToko ?: "Wijaya Suksma",
            phone = currentUserActive.value?.phone ?: "+628123456789",
            onBack = { currentScreen.value = "PENGATURAN" },
            onLogout = { currentScreen.value = "LOGIN" }
        )
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

@Composable
fun LoginScreen(dao: TitipKuDao, onLoginSuccess: (UserEntity) -> Unit, onRegisterClick: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(85.dp), tint = Color(0xFF1E88E5))
        Text("TitipKu", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Nama Pengguna") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Kata Sandi") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                scope.launch {
                    val dbUser = withContext(Dispatchers.IO) { dao.getUserByUsername(user) }
                    if (user == "ilham" && pass == "123") {
                        onLoginSuccess(UserEntity("ilham", "123", "Wijaya Suksma", "+628123456789"))
                    } else if (dbUser != null && dbUser.pass == pass) {
                        onLoginSuccess(dbUser)
                    } else {
                        Toast.makeText(context, "Username / Password Salah!", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Masuk", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onRegisterClick) { Text("Daftar Profil Baru") }
    }
}

@Composable
fun RegistrasiScreen(dao: TitipKuDao, onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC)).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackToLogin) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
            Text("Registrasi Usaha", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = store, onValueChange = { store = it }, label = { Text("Nama Toko") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if(user.isNotBlank() && pass.isNotBlank()){
                    scope.launch {
                        withContext(Dispatchers.IO) { dao.insertUser(UserEntity(user, pass, store, "")) }
                        Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Daftar Sekarang", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardView(namaToko: String, totalBarang: Int, totalTerjual: Int, totalKeuntungan: Int, onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Halo, $namaToko", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Keuntungan: Rp $totalKeuntungan")
                Text("Total Terjual: $totalTerjual")
                Text("Total Barang: $totalBarang")
            }
        }
        Button(onClick = { onNavigate("TRANSACTION_INPUT") }) { Text("Input Transaksi Baru") }
    }
}

@Composable
fun BarangView(list: List<BarangEntity>, onAddBarangClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Daftar Barang", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onAddBarangClick) { Icon(Icons.Default.Add, null) }
        }
        LazyColumn {
            items(list) { barang ->
                ListItem(headlineContent = { Text(barang.nama) }, supportingContent = { Text("Stok: ${barang.stok} | Rp ${barang.harga}") })
            }
        }
    }
}

@Composable
fun TokoView(list: List<TokoEntity>, onAddTokoClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mitra Toko", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onAddTokoClick) { Icon(Icons.Default.Add, null) }
        }
        LazyColumn {
            items(list) { toko ->
                ListItem(headlineContent = { Text(toko.nama) }, supportingContent = { Text("Status: ${toko.status}") })
            }
        }
    }
}

@Composable
fun LaporanView(totalKeuntungan: Int, produkTerlaris: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Laporan Penjualan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Total Keuntungan: Rp $totalKeuntungan", color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
        Text("Produk Terlaris: $produkTerlaris")
    }
}

@Composable
fun PengaturanView(username: String, namaToko: String, onProfileClick: () -> Unit, onLogout: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pengaturan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ListItem(
            headlineContent = { Text("Profil ($username)") },
            supportingContent = { Text("Usaha: $namaToko") },
            modifier = Modifier.clickable(onClick = onProfileClick),
            leadingContent = { Icon(Icons.Default.Person, null) }
        )
        ListItem(
            headlineContent = { Text("Logout") },
            modifier = Modifier.clickable(onClick = onLogout),
            leadingContent = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) }
        )
    }
}

@Composable
fun TransactionInputScreen(listToko: List<TokoEntity>, listBarang: List<BarangEntity>, onBack: () -> Unit, onSave: (TokoEntity, BarangEntity, Int) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Input Transaksi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (listBarang.isNotEmpty() && listToko.isNotEmpty()) {
            Button(onClick = { onSave(listToko[0], listBarang[0], 1) }, modifier = Modifier.padding(top = 16.dp)) { Text("Simpan Transaksi Cepat") }
        } else {
            Text("Data barang/toko kosong.", modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun AddDataScreen(onBack: () -> Unit, onSave: (String, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tambah Barang", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Button(onClick = { onSave(name, 5000, 10) }) { Text("Simpan") }
            TextButton(onClick = onBack) { Text("Batal") }
        }
    }
}

@Composable
fun AddTokoScreen(onBack: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tambah Toko Mitra", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Toko") }, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Button(onClick = { onSave(name) }) { Text("Simpan") }
            TextButton(onClick = onBack) { Text("Batal") }
        }
    }
}

@Composable
fun ProfileScreen(username: String, namaToko: String, phone: String, onBack: () -> Unit, onLogout: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Profil Pengguna", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Username: $username")
                Text("Toko: $namaToko")
                Text("Telepon: $phone")
            }
        }
        Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("Logout", color = Color.White)
        }
    }
}
