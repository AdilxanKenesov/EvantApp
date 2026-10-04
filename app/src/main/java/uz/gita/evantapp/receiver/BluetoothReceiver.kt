package uz.gita.evantapp.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * 1-USUL: ALOHIDA CLASS.
 *
 * BluetoothAdapter.ACTION_STATE_CHANGED — EXTRA_STATE ichida:
 * STATE_TURNING_ON -> STATE_ON, STATE_TURNING_OFF -> STATE_OFF.
 * Bu broadcastni qabul qilish uchun runtime permission kerak emas.
 */
class BluetoothReceiver(private val onChanged: (Boolean) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
        when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
            BluetoothAdapter.STATE_ON -> onChanged(true)
            BluetoothAdapter.STATE_OFF -> onChanged(false)
        }
    }

    companion object {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    }
}
