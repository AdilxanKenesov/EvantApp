package uz.gita.evantapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect

/**
 * 4-USUL: COMPOSE ICHIDA RECEIVER (LifecycleStartEffect).
 *
 * Receiver to'g'ridan-to'g'ri UI daraxtiga bog'lanadi:
 *  - ekran STARTED bo'lganda   -> register
 *  - STOPPED yoki composable ekrandan chiqib ketganda (onStopOrDispose) -> unregister
 *
 * Oddiy DisposableEffect yetarli emas: app fonga o'tganda composition yo'qolmaydi,
 * receiver esa ishlashda davom etib, fonda gapirib yuboradi. LifecycleStartEffect
 * esa Activity'ning onStart/onStop'iga moslashadi.
 *
 * rememberUpdatedState — receiver qayta yaratilmasdan doim eng so'nggi [onReceive] lambdasini chaqiradi.
 */
@Composable
fun SystemBroadcastEffect(
    vararg actions: String,
    onReceive: (Context, Intent) -> Unit,
) {
    val context = LocalContext.current
    val currentOnReceive by rememberUpdatedState(onReceive)

    LifecycleStartEffect(context, *actions) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Sticky broadcastning eski (saqlangan) nusxasi — o'zgarish emas, e'tiborsiz
                if (isInitialStickyBroadcast) return
                currentOnReceive(context, intent)
            }
        }
        val filter = IntentFilter().apply { actions.forEach(::addAction) }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        onStopOrDispose { context.unregisterReceiver(receiver) }
    }
}
