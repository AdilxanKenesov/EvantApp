package uz.gita.evantapp.model

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import uz.gita.evantapp.R

/**
 * Ekranda ko'rsatiladigan 10 ta tizim funksiyasi.
 *
 * [spokenOn]/[spokenOff] — holat o'zgarganda TTS aytadigan gap.
 * [settingsActions] — tile bosilganda ochiladigan Settings ekranlari (birinchi topilgani ochiladi).
 * Android 10+ da oddiy app Wi-Fi/Airplane kabi narsalarni o'zi yoqib-o'chira olmaydi,
 * shuning uchun foydalanuvchini tegishli tizim paneliga yuboramiz.
 * Bo'sh ro'yxat — funksiya faqat indikator (masalan, zaryadkani appdan "yoqib" bo'lmaydi).
 */
enum class SystemFeature(
    @StringRes val title: Int,
    @StringRes val spokenOn: Int,
    @StringRes val spokenOff: Int,
    @DrawableRes val icon: Int,
    val settingsActions: List<String>,
) {
    WIFI(
        R.string.feature_wifi, R.string.say_wifi_on, R.string.say_wifi_off, R.drawable.ic_wifi,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) listOf(Settings.Panel.ACTION_WIFI)
        else listOf(Settings.ACTION_WIFI_SETTINGS)
    ),
    BLUETOOTH(R.string.feature_bluetooth, R.string.say_bluetooth_on, R.string.say_bluetooth_off, R.drawable.ic_bluetooth, listOf(Settings.ACTION_BLUETOOTH_SETTINGS)),
    AIRPLANE(R.string.feature_airplane, R.string.say_airplane_on, R.string.say_airplane_off, R.drawable.ic_airplane, listOf(Settings.ACTION_AIRPLANE_MODE_SETTINGS)),
    INTERNET(
        R.string.feature_internet, R.string.say_internet_on, R.string.say_internet_off, R.drawable.ic_internet,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) listOf(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        else listOf(Settings.ACTION_WIRELESS_SETTINGS)
    ),
    LOCATION(R.string.feature_location, R.string.say_location_on, R.string.say_location_off, R.drawable.ic_location, listOf(Settings.ACTION_LOCATION_SOURCE_SETTINGS)),
    CHARGING(R.string.feature_charging, R.string.say_charging_on, R.string.say_charging_off, R.drawable.ic_charging, emptyList()),
    HEADSET(R.string.feature_headset, R.string.say_headset_on, R.string.say_headset_off, R.drawable.ic_headset, emptyList()),
    BATTERY_SAVER(R.string.feature_battery_saver, R.string.say_battery_saver_on, R.string.say_battery_saver_off, R.drawable.ic_battery_saver, listOf(Settings.ACTION_BATTERY_SAVER_SETTINGS)),
    DND(
        R.string.feature_dnd, R.string.say_dnd_on, R.string.say_dnd_off, R.drawable.ic_dnd,
        // ZEN_MODE_SETTINGS public API emas, topilmasa oddiy Sound settings ochiladi
        listOf("android.settings.ZEN_MODE_SETTINGS", Settings.ACTION_SOUND_SETTINGS)
    ),
    SILENT(R.string.feature_silent, R.string.say_silent_on, R.string.say_silent_off, R.drawable.ic_silent, listOf(Settings.ACTION_SOUND_SETTINGS)),
    NFC(R.string.feature_nfc, R.string.say_nfc_on, R.string.say_nfc_off, R.drawable.ic_nfc, listOf(Settings.ACTION_NFC_SETTINGS)),
    DATA_SAVER(
        R.string.feature_data_saver, R.string.say_data_saver_on, R.string.say_data_saver_off, R.drawable.ic_data_saver,
        // DATA_SAVER_SETTINGS public API emas, topilmasa Network settings ochiladi
        listOf("android.settings.DATA_SAVER_SETTINGS", Settings.ACTION_WIRELESS_SETTINGS)
    ),
    BATTERY_LOW(
        R.string.feature_battery_low, R.string.say_battery_low_on, R.string.say_battery_low_off, R.drawable.ic_battery_low,
        listOf(Intent.ACTION_POWER_USAGE_SUMMARY)
    ),
    USB(R.string.feature_usb, R.string.say_usb_on, R.string.say_usb_off, R.drawable.ic_usb, emptyList()),
}
