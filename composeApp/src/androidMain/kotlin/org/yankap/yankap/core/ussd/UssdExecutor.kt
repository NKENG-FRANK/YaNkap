package org.yankap.yankap.core.ussd

interface UssdExecutor {
    /**
     * Executes the given USSD code.
     * @param ussdCode The formatted USSD string to execute (e.g. "*126*1#")
     * @param simSlotIndex The index of the SIM slot to use (e.g., 0 for SIM1, 1 for SIM2). Optional.
     */
    fun executeUssd(ussdCode: String, simSlotIndex: Int? = null)
}
