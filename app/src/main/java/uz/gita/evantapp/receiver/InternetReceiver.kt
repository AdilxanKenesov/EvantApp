package uz.gita.evantapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import uz.gita.evantapp.model.SystemStateReader

/**
 * 1-USUL: ALOHIDA CLASS — DEPRECATED broadcast misoli.
 *
 * CONNECTIVITY_ACTION API 28 da deprecated bo'lgan va manifest orqali (Android 7+) kelmaydi,
 * lekin kod orqali register qilingan receiverga hali ham yetkaziladi.
 * Zamonaviy alternativa — ConnectivityManager.registerDefaultNetworkCallback(NetworkCallback).
 * Bu yerda o'rganish uchun broadcast varianti ishlatilgan.
 *
 * Intent extralari ham deprecated, shuning uchun holatni ConnectivityManager'dan qayta o'qiymiz.
 * Bu broadcast bir o'zgarishda bir necha marta kelishi mumkin — takrorlarni ViewModel filtrlaydi.
 */
class InternetReceiver(private val onChanged: (Boolean) -> Unit) : BroadcastReceiver() {

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ConnectivityManager.CONNECTIVITY_ACTION) return
        onChanged(SystemStateReader.isInternetAvailable(context))
    }

    companion object {
        @Suppress("DEPRECATION")
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }
}
