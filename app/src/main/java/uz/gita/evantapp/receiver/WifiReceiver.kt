package uz.gita.evantapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager

/**
 * 1-USUL: ALOHIDA CLASS — BroadcastReceiver'dan meros olib, onReceive() ni override qilamiz.
 *
 * Tizim Wi-Fi holati o'zgarganda WIFI_STATE_CHANGED_ACTION yuboradi.
 * Intent ichida EXTRA_WIFI_STATE: DISABLING -> DISABLED -> ENABLING -> ENABLED.
 * Oraliq holatlarni (…ING) e'tiborsiz qoldiramiz, aks holda ovoz ikki marta chalinadi.
 *
 * onReceive() MAIN thread'da ishlaydi — bu yerda og'ir ish qilinmaydi, faqat natijani uzatamiz.
 */
class WifiReceiver(private val onChanged: (Boolean) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WifiManager.WIFI_STATE_CHANGED_ACTION) return
        when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
            WifiManager.WIFI_STATE_ENABLED -> onChanged(true)
            WifiManager.WIFI_STATE_DISABLED -> onChanged(false)
        }
    }

    companion object {
        /** IntentFilter — receiver qaysi action'larni "eshitishini" belgilaydi */
        val filter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
    }
}
