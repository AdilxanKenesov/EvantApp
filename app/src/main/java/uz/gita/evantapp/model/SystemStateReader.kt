package uz.gita.evantapp.model

import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.BatteryManager
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

/**
 * Funksiyalarning JORIY holatini to'g'ridan-to'g'ri system service'lardan o'qiydi.
 *
 * Broadcast faqat "o'zgarish bo'ldi" deydi — app ochilgan paytdagi boshlang'ich holatni
 * bilish uchun esa shu yerdagidek manager'lardan so'rash kerak.
 */
object SystemStateReader {

    fun readAll(context: Context): Map<SystemFeature, Boolean> =
        SystemFeature.entries.associateWith { read(context, it) }

    fun read(context: Context, feature: SystemFeature): Boolean = when (feature) {
        SystemFeature.WIFI -> context.service<WifiManager>().isWifiEnabled
        SystemFeature.BLUETOOTH -> context.service<BluetoothManager>().adapter?.isEnabled == true
        SystemFeature.AIRPLANE -> isAirplaneOn(context)
        SystemFeature.INTERNET -> isInternetAvailable(context)
        SystemFeature.LOCATION -> LocationManagerCompat.isLocationEnabled(context.service<LocationManager>())
        SystemFeature.CHARGING -> isCharging(context)
        SystemFeature.HEADSET -> isWiredHeadsetConnected(context)
        SystemFeature.BATTERY_SAVER -> context.service<PowerManager>().isPowerSaveMode
        SystemFeature.DND -> isDndOn(context)
        SystemFeature.SILENT -> context.service<AudioManager>().ringerMode != AudioManager.RINGER_MODE_NORMAL
        SystemFeature.NFC -> isNfcOn(context)
        SystemFeature.DATA_SAVER -> isDataSaverOn(context)
        SystemFeature.BATTERY_LOW -> isBatteryLow(stickyIntent(context, Intent.ACTION_BATTERY_CHANGED))
        SystemFeature.USB -> isUsbConnected(stickyIntent(context, ACTION_USB_STATE))
    }

    fun isAirplaneOn(context: Context): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.service<ConnectivityManager>()
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun isDndOn(context: Context): Boolean {
        val filter = context.service<NotificationManager>().currentInterruptionFilter
        return filter != NotificationManager.INTERRUPTION_FILTER_ALL &&
            filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
    }

    /**
     * USB kabel holati. Bu action yashirin (@hide) SDK konstantasi, shuning uchun matn ko'rinishida.
     * Tizim uni STICKY qilib yuboradi, "connected" boolean extra bilan.
     */
    const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"

    private const val LOW_BATTERY_PERCENT = 15

    fun isNfcOn(context: Context): Boolean = NfcAdapter.getDefaultAdapter(context)?.isEnabled == true

    /** Data Saver yoqilgan bo'lsa status DISABLED emas (WHITELISTED — yoqilgan, lekin bizga ruxsat bor). */
    fun isDataSaverOn(context: Context): Boolean =
        context.service<ConnectivityManager>().restrictBackgroundStatus !=
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED

    fun isBatteryLow(battery: Intent?): Boolean {
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level < 0 || scale <= 0) return false
        return level * 100 / scale <= LOW_BATTERY_PERCENT
    }

    fun isUsbConnected(usbState: Intent?): Boolean = usbState?.getBooleanExtra("connected", false) == true

    private fun isCharging(context: Context): Boolean {
        val battery = stickyIntent(context, Intent.ACTION_BATTERY_CHANGED)
        return (battery?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0
    }

    /**
     * STICKY broadcast namunasi: receiver = null bilan register qilsak, tizim oxirgi saqlangan
     * intentni darhol qaytaradi — hech narsa ro'yxatda qolmaydi, unregister ham kerak emas.
     */
    private fun stickyIntent(context: Context, action: String): Intent? =
        ContextCompat.registerReceiver(context, null, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED)

    private fun isWiredHeadsetConnected(context: Context): Boolean =
        context.service<AudioManager>().getDevices(AudioManager.GET_DEVICES_OUTPUTS).any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
        }

    private inline fun <reified T : Any> Context.service(): T =
        ContextCompat.getSystemService(this, T::class.java)!!
}
