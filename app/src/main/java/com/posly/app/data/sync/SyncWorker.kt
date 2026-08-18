package com.posly.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.posly.app.data.local.dao.OrderDao
import com.posly.app.data.remote.SupabaseClientProvider
import com.posly.app.data.local.dao.SettingsDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that syncs PENDING orders to Supabase.
 * Triggered on network availability.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val orderDao: OrderDao,
    private val settingsDao: SettingsDao,
    private val supabaseClientProvider: SupabaseClientProvider
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settings = settingsDao.getStoreSettingsOnce()
                ?: return Result.success() // No settings yet

            if (!supabaseClientProvider.isConfigured(settings.supabaseUrl, settings.supabaseAnonKey)) {
                return Result.success() // Supabase not configured
            }

            val client = supabaseClientProvider.getClient(settings.supabaseUrl, settings.supabaseAnonKey)
            val pendingOrders = orderDao.getPendingSyncOrders().first()

            pendingOrders.forEach { order ->
                try {
                    client.from("orders").insert(mapOf(
                        "id" to order.id,
                        "invoice_number" to order.invoiceNumber,
                        "cashier_id" to order.cashierId.ifBlank { null },
                        "total_amount" to order.totalAmount,
                        "total_cost" to order.totalCost,
                        "discount_amount" to order.discountAmount,
                        "paid_amount" to order.paidAmount,
                        "change_amount" to order.changeAmount,
                        "payment_method" to order.paymentMethod,
                        "status" to order.status,
                        "void_reason" to order.voidReason,
                        "created_at" to order.createdAt
                    ))
                    orderDao.updateSyncStatus(order.id, "SYNCED")
                } catch (e: Exception) {
                    orderDao.updateSyncStatus(order.id, "FAILED")
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "posly_sync_worker"

        fun buildPeriodicRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

        fun buildOneTimeRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}
