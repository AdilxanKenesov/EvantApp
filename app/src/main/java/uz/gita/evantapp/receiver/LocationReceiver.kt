package uz.gita.evantapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

/**
 * 1-USUL: ALOHIDA CLASS.
 *
 * PROVIDERS_CHANGED_ACTION — GPS/Network provider yoqilsa yoki o'chirilsa keladi.
 * Intentda umumiy "yoqilgan/o'chirilgan" yo'q, shuning uchun LocationManager'dan so'raymiz.
 * (Bu broadcast manifest orqali qabul qilish mumkin bo'lgan kam sonli istisnolardan biri.)
 */
class LocationReceiver(private val onChanged: (Boolean) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != LocationManager.PROVIDERS_CHANGED_ACTION) return
        val manager = ContextCompat.getSystemService(context, LocationManager::class.java) ?: return
        onChanged(LocationManagerCompat.isLocationEnabled(manager))
    }

    companion object {
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
    }
}
