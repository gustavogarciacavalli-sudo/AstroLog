package br.edu.up.astrolog

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// =========================================================================
// REQUISITO: ROOM DATABASE COM 2 TABELAS E CRUD COMPLETO
// - Tabela 1: Local (locais de observação cadastrados pelo usuário)
// - Tabela 2: Observacao (registros do céu vinculados a um Local via ForeignKey)
// =========================================================================

// ---------- TABELA 1: locais de observação ----------
@Entity(tableName = "locais")
data class Local(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val latitude: Double,
    val longitude: Double
)

// ---------- TABELA 2: observações do céu (Chave Estrangeira -> locais) ----------
// onDelete = ForeignKey.CASCADE garante que, ao apagar um Local, todas as
// observações daquele local sejam apagadas automaticamente pelo banco.
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
    val objeto: String,          // ex: Saturno, Lua, Nebulosa de Órion
    val data: String,            // formato AAAA-MM-DD
    val notas: String,           // anotações livres do usuário
    val nota: Int,               // avaliação de 1 a 5 estrelas
    val condicaoCeu: String = "" // preenchido via API Open-Meteo ou manualmente
)

// ---------- DAO 1: CRUD completo da tabela "locais" ----------
// Flow<List<Local>> atualiza a tela automaticamente sempre que o banco muda (Read).
// As funções suspend rodam fora da thread principal para não travar a tela.
@Dao
interface LocalDao {
    @Query("SELECT * FROM locais ORDER BY nome")
    fun todos(): Flow<List<Local>>               // READ (Consulta reativa)

    @Insert
    suspend fun inserir(l: Local)                // CREATE (Inserção)

    @Update
    suspend fun atualizar(l: Local)              // UPDATE (Alteração)

    @Delete
    suspend fun excluir(l: Local)                // DELETE (Exclusão)
}

// ---------- DAO 2: CRUD completo da tabela "observacoes" ----------
@Dao
interface ObservacaoDao {
    @Query("SELECT * FROM observacoes ORDER BY id DESC")
    fun todas(): Flow<List<Observacao>>          // READ (Consulta reativa)

    @Insert
    suspend fun inserir(o: Observacao)           // CREATE (Inserção)

    @Update
    suspend fun atualizar(o: Observacao)         // UPDATE (Alteração)

    @Delete
    suspend fun excluir(o: Observacao)           // DELETE (Exclusão)
}

// ---------- Configuração do Banco de Dados Room (Singleton) ----------
@Database(entities = [Local::class, Observacao::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localDao(): LocalDao
    abstract fun observacaoDao(): ObservacaoDao

    companion object {
        @Volatile private var inst: AppDatabase? = null

        // Padrão Singleton: garante uma única instância do banco em todo o app
        fun get(c: Context): AppDatabase = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, AppDatabase::class.java, "astrolog.db")
                .build().also { inst = it }
        }
    }
}
