package br.edu.up.astrolog

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "locais")
data class Local(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val latitude: Double,
    val longitude: Double
)

@Entity(
    tableName = "observacoes",
    foreignKeys = [ForeignKey(
        entity = Local::class,
        parentColumns = ["id"],
        childColumns = ["localId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("localId")]
)
data class Observacao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localId: Long,
    val objeto: String,
    val data: String,
    val notas: String,
    val nota: Int,
    val condicaoCeu: String = ""
)

@Dao
interface LocalDao {
    @Query("SELECT * FROM locais ORDER BY nome")
    fun todos(): Flow<List<Local>>
    @Insert suspend fun inserir(l: Local)
    @Update suspend fun atualizar(l: Local)
    @Delete suspend fun excluir(l: Local)
}

@Dao
interface ObservacaoDao {
    @Query("SELECT * FROM observacoes ORDER BY id DESC")
    fun todas(): Flow<List<Observacao>>
    @Insert suspend fun inserir(o: Observacao)
    @Update suspend fun atualizar(o: Observacao)
    @Delete suspend fun excluir(o: Observacao)
}

@Database(entities = [Local::class, Observacao::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localDao(): LocalDao
    abstract fun observacaoDao(): ObservacaoDao

    companion object {
        @Volatile private var inst: AppDatabase? = null
        fun get(c: Context): AppDatabase = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, AppDatabase::class.java, "astrolog.db")
                .build().also { inst = it }
        }
    }
}
