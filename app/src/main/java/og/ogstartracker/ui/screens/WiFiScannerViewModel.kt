package og.ogstartracker.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WiFiScannerViewModel : ViewModel() {

	private val _uiState = MutableStateFlow(WiFiScannerUiState())
	val uiState = _uiState.asStateFlow()

	fun scanForTrackers(context: android.content.Context) {
		viewModelScope.launch {
			_uiState.update { it.copy(isScanning = true) }
			
			val trackers = og.ogstartracker.utils.WiFiHelper.scanForTrackers(context)
			
			_uiState.update { 
				it.copy(
					availableTrackers = trackers,
					isScanning = false
				) 
			}
		}
	}

	fun selectTracker(ssid: String) {
		_uiState.update { it.copy(selectedTracker = ssid) }
	}

	fun saveSelectedTracker(context: android.content.Context) {
		val selectedSSID = _uiState.value.selectedTracker ?: return
		
		// Save to preferences
		val preferences = context.getSharedPreferences(
			context.packageName + "_preferences",
			android.app.Activity.MODE_PRIVATE
		)
		preferences.edit()
			.putString(og.ogstartracker.Config.PREFERENCES_SELECTED_TRACKER_SSID, selectedSSID)
			.apply()
	}
}

data class WiFiScannerUiState(
	val availableTrackers: List<String> = emptyList(),
	val isScanning: Boolean = false,
	val selectedTracker: String? = null
)
