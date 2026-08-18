package com.posly.app.data.local.dao

import androidx.room.*
import com.posly.app.data.local.entity.ExpenseEntity
import com.posly.app.data.local.entity.ProfileEntity
import com.posly.app.data.local.entity.StoreSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE role = 'worker' AND isActive = 1")
    fun getAllWorkers(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE role = 'owner' LIMIT 1")
    suspend fun getOwner(): ProfileEntity?

    @Query("SELECT pinCode FROM profiles WHERE role = 'owner' LIMIT 1")
    suspend fun getOwnerPin(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("UPDATE profiles SET isActive = 0 WHERE id = :workerId")
    suspend fun deactivateWorker(workerId: String)

    @Query("DELETE FROM profiles WHERE id = :workerId AND role = 'worker'")
    suspend fun deleteWorker(workerId: String)

    @Query("SELECT COUNT(*) FROM profiles WHERE role = 'owner'")
    suspend fun countOwners(): Int
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses 
        WHERE createdAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalExpenses(startDate: Long, endDate: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)

    @Query("UPDATE expenses SET syncStatus = :status WHERE id = :expenseId")
    suspend fun updateSyncStatus(expenseId: String, status: String)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM store_settings WHERE id = 'singleton' LIMIT 1")
    fun getStoreSettings(): Flow<StoreSettingsEntity?>

    @Query("SELECT * FROM store_settings WHERE id = 'singleton' LIMIT 1")
    suspend fun getStoreSettingsOnce(): StoreSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStoreSettings(settings: StoreSettingsEntity)
}
