package uz.gita.evantapp.sound

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Holat o'zgarishini ovoz chiqarib aytadi: "Wi-Fi turned on", "Charger connected" ...
 *
 * Android'ning o'rnatilgan TextToSpeech (TTS) dvigateli ishlatiladi — ovoz fayllari kerak emas.
 * TTS asinxron ishga tushadi (onInit), shuning uchun tayyor bo'lgunicha kelgan gaplar
 * [pending] ro'yxatida kutib turadi.
 *
 * QUEUE_ADD — gaplar navbat bilan aytiladi. Masalan Airplane yoqilganda
 * "Airplane mode on", "Wi-Fi turned off", "Bluetooth turned off" ketma-ket eshitiladi.
 */
class Announcer(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    private val pending = mutableListOf<String>()
    private var ready = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        // App tili inglizcha — ingliz ovozi bo'lmasa, qurilmaning standart tili qoladi
        if (tts.isLanguageAvailable(Locale.US) >= TextToSpeech.LANG_AVAILABLE) {
            tts.language = Locale.US
        }
        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        ready = true
        pending.forEach(::speak)
        pending.clear()
    }

    fun speak(text: String) {
        if (!ready) {
            pending += text
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, text.hashCode().toString())
    }

    fun release() {
        tts.stop()
        tts.shutdown()
    }
}
