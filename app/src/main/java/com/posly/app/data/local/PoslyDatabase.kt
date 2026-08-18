package com.posly.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.posly.app.data.local.dao.*
import com.posly.app.data.local.entity.*

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
    exportSchema = false
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
            val builder = Room.databaseBuilder(context, PoslyDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
            runCatching {
                val clazz = Class.forName("net.sqlcipher.database.SupportFactory")
                val constructor = clazz.getConstructor(ByteArray::class.java)
                val factory = constructor.newInstance(passphrase) as androidx.sqlite.db.SupportSQLiteOpenHelper.Factory
                builder.openHelperFactory(factory)
            }
            return builder.build()
        }
    }
}
