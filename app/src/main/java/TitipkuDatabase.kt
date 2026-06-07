package com.example.titipku

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. DATABASE ENTITIES (Mendukung Kolom URL Gambar)
@Entity(tableName = "table_user")
data class UserEntity(
    @PrimaryKey val username: String,
    val pass: String,
    val namaToko: String,
    val phone: String
)

@Entity(tableName = "table_barang")
data class BarangEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val harga: Int,
    val stok: Int,
    val status: String,
    val urlGambar: String
)

@Entity(tableName = "table_toko")
data class TokoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val jmlBarang: Int,
    val status: String,
    val urlGambar: String
)

// 2. DATA ACCESS OBJECT (DAO)
@Dao
interface TitipKuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM table_user WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM table_barang")
    fun getAllBarang(): Flow<List<BarangEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarang(barang: BarangEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialBarang(barang: List<BarangEntity>)

    @Query("SELECT COUNT(*) FROM table_barang")
    suspend fun getBarangCount(): Int

    @Query("SELECT * FROM table_toko")
    fun getAllToko(): Flow<List<TokoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToko(toko: TokoEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialToko(toko: List<TokoEntity>)

    @Query("SELECT COUNT(*) FROM table_toko")
    suspend fun getTokoCount(): Int
}

// 3. APPDATABASE LAYER
@Database(entities = [UserEntity::class, BarangEntity::class, TokoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun titipKuDao(): TitipKuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "titipku_db_hybrid"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. DATA MODEL NETWORKING (Untuk Map JSON Git Gist ke Data Class)
data class GistApiResponse(
    val daftar_toko: List<RemoteToko>,
    val daftar_barang: List<RemoteBarang>
)

data class RemoteToko(
    val id: Int,
    val nama: String,
    val jmlBarang: Int,
    val status: String,
    val url_gambar: String
)

data class RemoteBarang(
    val id: Int,
    val nama: String,
    val harga: Int,
    val stok: Int,
    val status: String,
    val url_gambar: String
)