package com.posly.app.domain.usecase.pos

import com.posly.app.domain.repository.AuthRepository
import com.posly.app.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Use case: void an order.
 * Workers require Owner PIN authorization.
 * Owners can void directly.
 */
class VoidOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        orderId: String,
        reason: String,
        ownerPin: String? = null
    ): Result<Unit> = runCatching {
        val currentRole = authRepository.getCurrentUserRole()
            ?: error("Not authenticated")

        val approvedBy: String

        when {
            currentRole == com.posly.app.domain.model.UserRole.OWNER -> {
                // Owner can void directly — get their profile ID
                approvedBy = kotlinx.coroutines.flow.first(authRepository.currentProfile)?.id
                    ?: error("Owner profile not found")
            }
            ownerPin != null -> {
                // Worker submitting with owner PIN
                val pinValid = authRepository.verifyOwnerPin(ownerPin).getOrThrow()
                if (!pinValid) error("PIN Owner tidak valid. Void dibatalkan.")
                approvedBy = "owner_pin_verified"
            }
            else -> {
                error("Worker harus memasukkan PIN Owner untuk membatalkan transaksi.")
            }
        }

        if (reason.isBlank()) error("Alasan void wajib diisi")

        orderRepository.voidOrder(
            orderId = orderId,
            reason = reason,
            approvedBy = approvedBy
        ).getOrThrow()
    }
}
