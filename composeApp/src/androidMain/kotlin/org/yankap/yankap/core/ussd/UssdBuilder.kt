package org.yankap.yankap.core.ussd

import org.yankap.yankap.domain.models.Operator
import org.yankap.yankap.domain.models.UssdOperation

object UssdBuilder {
    
    /**
     * Formats a USSD string based on the operator, operation, amount, and recipient.
     * Note: These formats are placeholders and should be updated with actual Cameroon USSD structures.
     */
    fun buildUssdString(
        operator: Operator,
        operation: UssdOperation,
        amount: String? = null,
        recipient: String? = null,
        pin: String? = null
    ): String {
        return when (operator) {
            Operator.MTN -> buildMtnString(operation, amount, recipient, pin)
            Operator.ORANGE -> buildOrangeString(operation, amount, recipient, pin)
            Operator.CAMTEL -> buildCamtelString(operation, amount, recipient, pin)
            Operator.NEXTTEL -> buildNexttelString(operation, amount, recipient, pin)
        }
    }

    private fun buildMtnString(operation: UssdOperation, amount: String?, recipient: String?, pin: String?): String {
        return when (operation) {
            UssdOperation.SEND_MONEY -> "*126*1*1*$recipient*$amount*${pin ?: ""}#" // e.g., *126*1*1*67XXXXXXX*1000*PIN#
            UssdOperation.WITHDRAW_MONEY -> "*126*2*1*$recipient*$amount*${pin ?: ""}#"
            UssdOperation.CHECK_BALANCE -> "*126*1*2*${pin ?: ""}#" // Placeholder
            else -> "*126#" 
        }
    }

    private fun buildOrangeString(operation: UssdOperation, amount: String?, recipient: String?, pin: String?): String {
        return when (operation) {
            UssdOperation.SEND_MONEY -> "*150*1*1*$recipient*$amount*${pin ?: ""}#" // Placeholder format
            UssdOperation.CHECK_BALANCE -> "*150*6*${pin ?: ""}#" // Placeholder
            else -> "*150#"
        }
    }

    private fun buildCamtelString(operation: UssdOperation, amount: String?, recipient: String?, pin: String?): String {
        return "*160#" // Needs implementation
    }

    private fun buildNexttelString(operation: UssdOperation, amount: String?, recipient: String?, pin: String?): String {
        return "*282#" // Needs implementation
    }
}
