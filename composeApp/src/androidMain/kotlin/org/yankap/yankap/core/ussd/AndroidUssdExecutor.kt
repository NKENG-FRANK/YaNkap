package org.yankap.yankap.core.ussd

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager

class AndroidUssdExecutor(private val context: Context) : UssdExecutor {

    override fun executeUssd(ussdCode: String, simSlotIndex: Int?) {
        // Prepare the USSD code for the dialer by encoding the '#' symbol
        val encodedHashString = Uri.encode("#")
        val finalCode = ussdCode.replace("#", encodedHashString)
        val uri = Uri.parse("tel:$finalCode")
        val intent = Intent(Intent.ACTION_CALL, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // If a specific SIM is selected, try to find the PhoneAccountHandle to attach to the intent
        if (simSlotIndex != null) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (telecomManager != null) {
                try {
                    // Requires Manifest.permission.READ_PHONE_STATE 
                    val accountHandles = telecomManager.callCapablePhoneAccounts
                    if (simSlotIndex < accountHandles.size) {
                        val simHandle: PhoneAccountHandle = accountHandles[simSlotIndex]
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, simHandle)
                    }
                } catch (e: SecurityException) {
                    // Handle missing permissions case
                    e.printStackTrace()
                }
            }
        }

        try {
            // Note: Requires Manifest.permission.CALL_PHONE permission
            context.startActivity(intent)
        } catch (e: SecurityException) {
            // Handle missing permissions case
            e.printStackTrace()
        }
    }
}
