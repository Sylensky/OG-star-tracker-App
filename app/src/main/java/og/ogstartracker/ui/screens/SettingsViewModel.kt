package og.ogstartracker.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import og.ogstartracker.domain.models.Hemisphere
import og.ogstartracker.domain.usecases.arduino.GetVersionUseCase
import og.ogstartracker.network.models.VersionResponse
import og.ogstartracker.utils.onSuccess

class SettingsViewModel internal constructor(
	private val getVersion: GetVersionUseCase,
) : ViewModel() {

	private val _uiState = MutableStateFlow(SettingsUiState())
	val uiState = _uiState.asStateFlow()

	init {
		viewModelScope.launch(Dispatchers.Default) {
			// Use cached version if available
			val cached = og.ogstartracker.utils.VersionHolder.version
			if (cached != null) {
				_uiState.update { it.copy(version = cached) }
			} else {
				getVersion().onSuccess { versionResponse ->
					_uiState.update { it.copy(version = versionResponse) }
				}
			}
		}
	}

}

data class SettingsUiState internal constructor(
	val hemisphere: Hemisphere? = null,
	val version: VersionResponse? = null,
)
