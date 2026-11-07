package in.entekadam.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val side: String, // RECEIVE or PAY
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)

@Entity(tableName = "debts", foreignKeys = [ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.RESTRICT)], indices = [Index("personId")])
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val amountPaise: Long,
    val date: Long,
    val description: String = "",
    val interestRate: Double? = null,
    val dueDate: Long? = null,
    val reminderAt: Long? = null,
    val reminderEnabled: Boolean = false,
    val archived: Boolean = false
)

@Entity(tableName = "payments", foreignKeys = [ForeignKey(entity = Debt::class, parentColumns = ["id"], childColumns = ["debtId"], onDelete = ForeignKey.RESTRICT)], indices = [Index("debtId")])
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val amountPaise: Long,
    val date: Long,
    val method: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_log")
data class AuditEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao interface FinanceDao {
    @Query("SELECT * FROM people WHERE archived = 0 ORDER BY name COLLATE NOCASE") fun people(): Flow<List<Person>>
    @Query("SELECT * FROM people WHERE id = :id") suspend fun person(id: Long): Person?
    @Insert suspend fun insertPerson(person: Person): Long
    @Update suspend fun updatePerson(person: Person)
    @Query("UPDATE people SET archived = 1 WHERE id = :id") suspend fun archivePerson(id: Long)

    @Query("SELECT * FROM debts WHERE archived = 0 ORDER BY date DESC") fun debts(): Flow<List<Debt>>
    @Query("SELECT * FROM debts WHERE personId = :personId AND archived = 0 ORDER BY date DESC") fun debtsForPerson(personId: Long): Flow<List<Debt>>
    @Query("SELECT * FROM debts WHERE id = :id") suspend fun debt(id: Long): Debt?
    @Insert suspend fun insertDebt(debt: Debt): Long
    @Update suspend fun updateDebt(debt: Debt)
    @Query("UPDATE debts SET archived = 1 WHERE id = :id") suspend fun archiveDebt(id: Long)

    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC") fun payments(): Flow<List<Payment>>
    @Query("SELECT * FROM payments WHERE debtId = :debtId ORDER BY date DESC, id DESC") fun paymentsForDebt(debtId: Long): Flow<List<Payment>>
    @Query("SELECT * FROM payments WHERE debtId IN (:debtIds) ORDER BY date DESC, id DESC") suspend fun paymentsForDebts(debtIds: List<Long>): List<Payment>
    @Insert suspend fun insertPayment(payment: Payment): Long
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC") fun auditLog(): Flow<List<AuditEntry>>
    @Insert suspend fun insertAudit(entry: AuditEntry)

    @Query("SELECT * FROM people") suspend fun allPeople(): List<Person>
    @Query("SELECT * FROM debts") suspend fun allDebts(): List<Debt>
    @Query("SELECT * FROM payments") suspend fun allPayments(): List<Payment>
    @Query("SELECT * FROM audit_log") suspend fun allAudit(): List<AuditEntry>
    @Query("DELETE FROM payments") suspend fun clearPayments()
    @Query("DELETE FROM debts") suspend fun clearDebts()
    @Query("DELETE FROM people") suspend fun clearPeople()
    @Query("DELETE FROM audit_log") suspend fun clearAudit()
    @Insert suspend fun insertPeople(items: List<Person>)
    @Insert suspend fun insertDebts(items: List<Debt>)
    @Insert suspend fun insertPayments(items: List<Payment>)
    @Insert suspend fun insertAuditItems(items: List<AuditEntry>)
}

@Database(entities = [Person::class, Debt::class, Payment::class, AuditEntry::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): FinanceDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: android.content.Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ente_kadam.db").build().also { INSTANCE = it }
        }
    }
}
