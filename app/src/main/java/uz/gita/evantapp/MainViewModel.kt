package uz.gita.evantapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.gita.evantapp.model.SystemFeature
import uz.gita.evantapp.model.SystemStateReader
import uz.gita.evantapp.sound.Announcer

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val announcer = Announcer(application)

    private val _states = MutableStateFlow(SystemStateReader.readAll(application))
    val states: StateFlow<Map<SystemFeature, Boolean>> = _states.asStateFlow()

    /**
     * Receiverlardan keladigan hodisa. Ovozli xabar faqat holat HAQIQATAN o'zgarganda chalinadi —
     * ba'zi broadcastlar (CONNECTIVITY_ACTION) bir o'zgarish uchun bir necha marta keladi.
     */
    fun onSystemEvent(feature: SystemFeature, isOn: Boolean) {
        if (_states.value[feature] == isOn) return
        _states.update { it + (feature to isOn) }
        announcer.speak(getApplication<Application>().getString(if (isOn) feature.spokenOn else feature.spokenOff))
    }

    /** App fonda bo'lganda receiverlar o'chiq — qaytganda holatni ovozsiz yangilaymiz. */
    fun refresh() {
        _states.value = SystemStateReader.readAll(getApplication())
    }

    override fun onCleared() = announcer.release()
}
