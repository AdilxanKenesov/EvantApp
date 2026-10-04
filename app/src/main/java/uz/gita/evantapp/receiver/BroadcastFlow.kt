package uz.gita.evantapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * 3-USUL: BROADCAST'NI KOTLIN FLOW'GA O'RASH (callbackFlow).
 *
 * Zamonaviy usul: receiver Flow ichida yashirin. Kimdir collect qila boshlasa — register,
 * collect to'xtasa (masalan repeatOnLifecycle STOPPED holatda) — awaitClose ichida unregister.
 * Shu tufayli unregister qilishni unutib, memory leak qilish ehtimoli yo'q.
 *
 * [skipInitialSticky] — STICKY broadcastlar (masalan ACTION_HEADSET_PLUG) register qilingan
 * zahoti oxirgi saqlangan intentni yuboradi. isInitialStickyBroadcast() bilan uni ajratib olamiz,
 * aks holda app ochilishi bilan ovoz chalinib ketadi.
 */
fun Context.broadcastFlow(
    filter: IntentFilter,
    skipInitialSticky: Boolean = true,
): Flow<Intent> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (skipInitialSticky && isInitialStickyBroadcast) return
            trySend(intent)
        }
    }
    // RECEIVER_NOT_EXPORTED: boshqa app'lar bu receiverga broadcast yubora olmaydi.
    // System broadcastlar baribir yetib keladi. Android 14+ da flag ko'rsatish majburiy.
    ContextCompat.registerReceiver(this@broadcastFlow, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    awaitClose { unregisterReceiver(receiver) }
}
