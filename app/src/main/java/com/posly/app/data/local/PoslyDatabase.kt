package com.posly.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.posly.app.data.local.dao.*
import com.posly.app.data.local.entity.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        ProfileEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        DraftOrderEntity::class,
        ExpenseEntity::class,
        StoreSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PoslyDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        private const val DB_NAME = "posly_db"

        fun create(context: Context, passphrase: ByteArray): PoslyDatabase {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(context, PoslyDatabase::class.java, DB_NAME)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigrationFrom()
                .build()
        }
    }
}
