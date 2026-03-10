package org.yankap.yankap.domain.models

data class Transaction(
    val id: String,
    val operator: Operator,
    val operation: UssdOperation,
    val amount: Double?,
    val recipientNumber: String?,
    val timestamp: Long,
    val status: TransactionStatus
)

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED
}
