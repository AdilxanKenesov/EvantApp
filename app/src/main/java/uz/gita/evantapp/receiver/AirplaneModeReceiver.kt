package uz.gita.evantapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * 1-USUL: ALOHIDA CLASS.
 *
 * ACTION_AIRPLANE_MODE_CHANGED — intent ichida "state" boolean extra keladi.
 * Bu "protected" broadcast: uni faqat tizim yubora oladi, boshqa app soxtalashtira olmaydi.
 */
class AirplaneModeReceiver(private val onChanged: (Boolean) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_AIRPLANE_MODE_CHANGED) return
        onChanged(intent.getBooleanExtra("state", false))
    }

    companion object {
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
    }
}
