package uz.gita.evantapp

import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import uz.gita.evantapp.model.SystemFeature
import uz.gita.evantapp.model.SystemStateReader
import uz.gita.evantapp.receiver.AirplaneModeReceiver
import uz.gita.evantapp.receiver.BluetoothReceiver
import uz.gita.evantapp.receiver.InternetReceiver
import uz.gita.evantapp.receiver.LocationReceiver
import uz.gita.evantapp.receiver.SystemBroadcastEffect
import uz.gita.evantapp.receiver.WifiReceiver
import uz.gita.evantapp.receiver.broadcastFlow
import uz.gita.evantapp.ui.MainScreen
import uz.gita.evantapp.ui.theme.EvantAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // ---------------------------------------------------------------------
    // 1-USUL: alohida class'lar. Har biri o'z IntentFilter'i bilan juftlanadi.
    // ---------------------------------------------------------------------
    private val classReceivers: List<Pair<BroadcastReceiver, IntentFilter>> by lazy {
        listOf(
            WifiReceiver { viewModel.onSystemEvent(SystemFeature.WIFI, it) } to WifiReceiver.filter,
            BluetoothReceiver { viewModel.onSystemEvent(SystemFeature.BLUETOOTH, it) } to BluetoothReceiver.filter,
            AirplaneModeReceiver { viewModel.onSystemEvent(SystemFeature.AIRPLANE, it) } to AirplaneModeReceiver.filter,
            InternetReceiver { viewModel.onSystemEvent(SystemFeature.INTERNET, it) } to InternetReceiver.filter,
            LocationReceiver { viewModel.onSystemEvent(SystemFeature.LOCATION, it) } to LocationReceiver.filter,
        )
    }

    // ---------------------------------------------------------------------
    // 2-USUL: anonim object. Kichik, faqat shu Activity'da kerak bo'lgan receiverlar uchun qulay.
    // ---------------------------------------------------------------------

    /** Bitta receiver — ikkita action: filter'ga ikkalasi qo'shiladi, onReceive ichida ajratiladi. */
    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> viewModel.onSystemEvent(SystemFeature.CHARGING, true)
                Intent.ACTION_POWER_DISCONNECTED -> viewModel.onSystemEvent(SystemFeature.CHARGING, false)
            }
        }
    }
    private val chargingFilter = IntentFilter().apply {
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
    }

    /** Intentda holat yo'q — faqat "o'zgardi" signali. Haqiqiy qiymat PowerManager'dan olinadi. */
    private val batterySaverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val power = ContextCompat.getSystemService(context, PowerManager::class.java) ?: return
            viewModel.onSystemEvent(SystemFeature.BATTERY_SAVER, power.isPowerSaveMode)
        }
    }
    private val batterySaverFilter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        collectFlowReceivers()

        setContent {
            EvantAppTheme {
                val states by viewModel.states.collectAsStateWithLifecycle()
                ComposeReceivers()
                MainScreen(states = states, onFeatureClick = ::openSettings)
            }
        }
    }

    /**
     * onStart <-> onStop juftligi: ekran ko'rinib turgan paytdagina eshitamiz.
     * Register qilishdan oldin holatni yangilaymiz — fonda bo'lgan o'zgarishlar ovozsiz qo'llanadi.
     */
    override fun onStart() {
        super.onStart()
        viewModel.refresh()

        // ContextCompat.registerReceiver + RECEIVER_NOT_EXPORTED: Android 14+ talabi,
        // eski versiyalarda ham bir xil ishlaydi. System broadcastlar baribir yetib keladi.
        classReceivers.forEach { (receiver, filter) -> register(receiver, filter) }
        register(chargingReceiver, chargingFilter)
        register(batterySaverReceiver, batterySaverFilter)
    }

    override fun onStop() {
        // Unregister qilinmasa — Activity leak va fonda keraksiz ovozlar.
        classReceivers.forEach { (receiver, _) -> unregisterReceiver(receiver) }
        unregisterReceiver(chargingReceiver)
        unregisterReceiver(batterySaverReceiver)
        super.onStop()
    }

    private fun register(receiver: BroadcastReceiver, filter: IntentFilter) {
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    // ---------------------------------------------------------------------
    // 3-USUL: callbackFlow. repeatOnLifecycle(STARTED) — onStart'da collect boshlanadi (= register),
    // onStop'da bekor qilinadi (= awaitClose ichida unregister). Qo'lda unregister yozilmaydi.
    // ---------------------------------------------------------------------
    private fun collectFlowReceivers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // ACTION_HEADSET_PLUG — STICKY broadcast; boshlang'ich intent broadcastFlow ichida tashlab yuboriladi.
                // "state" extra: 1 = ulangan, 0 = uzilgan.
                launch {
                    broadcastFlow(IntentFilter(AudioManager.ACTION_HEADSET_PLUG)).collect { intent ->
                        viewModel.onSystemEvent(SystemFeature.HEADSET, intent.getIntExtra("state", 0) == 1)
                    }
                }
                launch {
                    broadcastFlow(IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)).collect {
                        viewModel.onSystemEvent(SystemFeature.DND, SystemStateReader.isDndOn(this@MainActivity))
                    }
                }
                launch {
                    broadcastFlow(IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)).collect { intent ->
                        val mode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)
                        viewModel.onSystemEvent(SystemFeature.SILENT, mode != AudioManager.RINGER_MODE_NORMAL)
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // 4-USUL: Compose ichida LifecycleStartEffect (receiver/ComposeBroadcast.kt).
    // ---------------------------------------------------------------------
    @Composable
    private fun ComposeReceivers() {
        SystemBroadcastEffect(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) { _, intent ->
            when (intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)) {
                NfcAdapter.STATE_ON -> viewModel.onSystemEvent(SystemFeature.NFC, true)
                NfcAdapter.STATE_OFF -> viewModel.onSystemEvent(SystemFeature.NFC, false)
            }
        }
        // Faqat kod orqali register qilingan receiverlarga keladi (manifestga kelmaydi)
        SystemBroadcastEffect(ConnectivityManager.ACTION_RESTRICT_BACKGROUND_CHANGED) { context, _ ->
            viewModel.onSystemEvent(SystemFeature.DATA_SAVER, SystemStateReader.isDataSaverOn(context))
        }
        // ACTION_BATTERY_CHANGED — sticky va juda tez-tez keladi (har 1% da);
        // ViewModel faqat "past/normal" chegarasi kesib o'tilganda gapiradi
        SystemBroadcastEffect(Intent.ACTION_BATTERY_CHANGED) { _, intent ->
            viewModel.onSystemEvent(SystemFeature.BATTERY_LOW, SystemStateReader.isBatteryLow(intent))
        }
        SystemBroadcastEffect(SystemStateReader.ACTION_USB_STATE) { _, intent ->
            viewModel.onSystemEvent(SystemFeature.USB, SystemStateReader.isUsbConnected(intent))
        }
    }

    private fun openSettings(feature: SystemFeature) {
        for (action in feature.settingsActions) {
            try {
                startActivity(Intent(action))
                return
            } catch (_: ActivityNotFoundException) {
                // keyingi variantni sinab ko'ramiz
            }
        }
    }
}
