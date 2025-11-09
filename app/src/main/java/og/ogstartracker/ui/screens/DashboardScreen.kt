package og.ogstartracker.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_WIFI_SETTINGS
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.LocalPreferenceFlow
import me.zhanghai.compose.preference.ProvidePreferenceFlow
import og.ogstartracker.Config
import og.ogstartracker.Config.PREFERENCES_HEMISPHERE
import og.ogstartracker.Config.PREFERENCES_TRACKING_MODE
import og.ogstartracker.Config.SCREEN_SETTINGS
import og.ogstartracker.R
import og.ogstartracker.domain.events.ExpositionTesterEvent
import og.ogstartracker.domain.events.PhotoControlEvent
import og.ogstartracker.domain.events.SlewControlEvent
import og.ogstartracker.domain.models.CheckListItem
import og.ogstartracker.domain.models.Hemisphere
import og.ogstartracker.domain.models.TrackingMode
import og.ogstartracker.domain.usecases.settings.SettingItem
import og.ogstartracker.ui.components.InfoDialog
import og.ogstartracker.ui.components.cards.ChecklistCard
import og.ogstartracker.ui.components.cards.ConnectionCard
import og.ogstartracker.ui.components.cards.ExpositionTesterCard
import og.ogstartracker.ui.components.cards.PhotoControlCard
import og.ogstartracker.ui.components.cards.SiderealCard
import og.ogstartracker.ui.components.cards.SlewControlCard
import og.ogstartracker.ui.components.common.Divider
import og.ogstartracker.ui.components.common.LocalInsets
import og.ogstartracker.ui.theme.AppTheme
import og.ogstartracker.ui.theme.BigGeneralIconSize
import og.ogstartracker.ui.theme.DimensNormal100
import og.ogstartracker.ui.theme.DimensNormal200
import og.ogstartracker.ui.theme.DimensNormal75
import og.ogstartracker.ui.theme.DimensSmall100
import og.ogstartracker.ui.theme.DimensSmall50
import og.ogstartracker.ui.theme.textStyle20Bold
import og.ogstartracker.utils.HardwareStatusService
import og.ogstartracker.utils.SystemUiHelper
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

private const val RESET_TRACKING_DELAY = 300L

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
	navController: NavController,
	viewModel: DashboardViewModel = koinViewModel(),
) {
	// setup system icon colors
	SystemUiHelper(statusIconsLight = true, navigationIconsLight = true)

	val context = LocalContext.current

	val uiState by viewModel.uiState.collectAsState()

	val permissionList = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		permissionList.add(Manifest.permission.POST_NOTIFICATIONS)
	}

	val fineLocationPermissionState = rememberMultiplePermissionsState(permissionList)

	val checkWifi by viewModel.checkWifiEvent.collectAsState()
	if (checkWifi) {
		checkLocationPermission(viewModel, fineLocationPermissionState, context)

		viewModel.resetWifiEvent()
	}

	InitLifecycleListener(
		onResume = {
			viewModel.startWiFiTimer()
			checkLocationPermission(viewModel, fineLocationPermissionState, context)
		},
		onStop = viewModel::stopWiFiTimer
	)

	// Start foreground service when connected to tracker API
	LaunchedEffect(uiState.wifiConnected) {
		if (uiState.wifiConnected) {
			val serviceIntent = Intent(context, HardwareStatusService::class.java)
			Timber.d("DashboardScreen: wifiConnected=true, starting HardwareStatusService")
			context.startForegroundService(serviceIntent)
		}
	}

	var showInfoDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	if (uiState.shouldShowOnboardingDialog) {
		showInfoDialog = true
	}

	LaunchedEffect(Unit) {
		// preload last user settings
		scope.launch {
			viewModel.settingsItemsFlow.first()
		}
	}

	DashboardScreenContent(
		uiState = uiState,
		onChecklistClicked = viewModel::changeChecklist,
		onSiderealClicked = viewModel::changeTracking,
		onSlewControlEvent = viewModel::slewControlEvent,
		onPhotoControlEvent = viewModel::photoControlEvent,
		onGearClick = {
			navController.navigate(SCREEN_SETTINGS)
		},
		onInfoClick = {
			showInfoDialog = true
		},
		notifyAboutChange = viewModel::notifyCacheAboutChange,
		onConnectionClick = {
			if (uiState.haveNotificationPermission &&
				uiState.haveLocationPermission &&
				uiState.wifiConnected
			) return@DashboardScreenContent

			when {
				// user did not grant location permission yet, request it
				!fineLocationPermissionState.allPermissionsGranted -> {
					// Check if permanently denied (can't show rationale anymore)
					val permanentlyDenied = fineLocationPermissionState.permissions.any { 
						it.status is PermissionStatus.Denied && !it.status.shouldShowRationale 
					}
					
					if (permanentlyDenied) {
						// Navigate to app settings to manually enable permission
						context.startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
							setData(Uri.fromParts("package", context.packageName, null))
						})
					} else {
						// Show permission request dialog
						fineLocationPermissionState.launchMultiplePermissionRequest()
					}
				}

				// user enabled location, but is on wrong wifi, open WiFi settings
				else -> {
					context.startActivity(Intent(ACTION_WIFI_SETTINGS))
				}
			}
		},
		onChecklistItemClicked = viewModel::updateCheckListItem,
		onExpositionTesterEvent = viewModel::expositionTesterEvent
	)

	if (showInfoDialog) {
		InfoDialog(onHide = {
			viewModel.setUserSawOnboard()
			showInfoDialog = false
		})
	}
}

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
private fun checkLocationPermission(
	viewModel: DashboardViewModel,
	fineLocationPermissionStates: MultiplePermissionsState,
	context: Context
) {
	viewModel.setHaveNotificationPermission(fineLocationPermissionStates.permissions.firstOrNull {
		it.permission == Manifest.permission.POST_NOTIFICATIONS
	}?.status?.isGranted == true)

	val haveLocationPermission = fineLocationPermissionStates.permissions.firstOrNull {
		it.permission == Manifest.permission.ACCESS_FINE_LOCATION
	}?.status?.isGranted == true

	viewModel.setHaveLocationPermission(haveLocationPermission)

	if (haveLocationPermission) {
		// detect if user is on the correct wifi
		checkWifiConnection(context, viewModel)
	}
}

private fun checkWifiConnection(
	context: Context,
	viewModel: DashboardViewModel
): Boolean {
	val connectivityManager = context.getSystemService<ConnectivityManager>() ?: return false
	val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

	// Check if WiFi is active (not cellular or other transport)
	val isOnWifi = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
	
	if (!isOnWifi) {
		Timber.d("DashboardScreen: Not connected to WiFi")
		viewModel.setConnection(false)
		return false
	}

	// WiFi is active - check if it's the tracker's hotspot
	val isTrackerWifi = og.ogstartracker.utils.WiFiHelper.isConnectedToTracker(context)
	Timber.d("DashboardScreen: On WiFi, isTrackerSSID=$isTrackerWifi")
	
	// Don't set connection=true here; let ViewModel's API check determine true connectivity
	// This just helps with UI feedback about WiFi status
	return isTrackerWifi
}

@Composable
private fun DashboardScreenContent(
	uiState: DashboardUiState,
	onChecklistClicked: () -> Unit,
	onSiderealClicked: (Boolean) -> Unit,
	onSlewControlEvent: (SlewControlEvent) -> Unit,
	onChecklistItemClicked: (CheckListItem) -> Unit,
	onExpositionTesterEvent: (ExpositionTesterEvent) -> Unit,
	onPhotoControlEvent: (PhotoControlEvent) -> Unit,
	onGearClick: () -> Unit,
	onConnectionClick: () -> Unit,
	notifyAboutChange: (SettingItem, Int?) -> Unit,
	onInfoClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Scaffold(
		modifier = modifier,
		content = { paddings ->
			DashboardScreenLayout(
				modifier = Modifier.padding(top = paddings.calculateTopPadding()),
				uiState = uiState,
				onChecklistClicked = onChecklistClicked,
				onSiderealClicked = onSiderealClicked,
				onSlewControlEvent = onSlewControlEvent,
				onPhotoControlEvent = onPhotoControlEvent,
				onGearClick = onGearClick,
				onInfoClick = onInfoClick,
				notifyAboutChange = notifyAboutChange,
				onConnectionClick = onConnectionClick,
				onChecklistItemClicked = onChecklistItemClicked,
				onExpositionTesterEvent = onExpositionTesterEvent,
			)
		},
		containerColor = MaterialTheme.colorScheme.surface,
	)
}

@Composable
private fun DashboardScreenLayout(
	uiState: DashboardUiState,
	onChecklistClicked: () -> Unit,
	onSlewControlEvent: (SlewControlEvent) -> Unit,
	onPhotoControlEvent: (PhotoControlEvent) -> Unit,
	onExpositionTesterEvent: (ExpositionTesterEvent) -> Unit,
	onChecklistItemClicked: (CheckListItem) -> Unit,
	onGearClick: () -> Unit,
	onConnectionClick: () -> Unit,
	notifyAboutChange: (SettingItem, Int?) -> Unit,
	onInfoClick: () -> Unit,
	onSiderealClicked: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
) {
	val state = rememberLazyListState()

	ProvidePreferenceFlow {
		val trackingModeValue = LocalPreferenceFlow.current.value[PREFERENCES_TRACKING_MODE]
			?: stringResource(TrackingMode.SIDEREAL.text)

		val hemisphere = LocalPreferenceFlow.current.value[PREFERENCES_HEMISPHERE]
			?: stringResource(Hemisphere.NORTH.text)

		LaunchedEffect(trackingModeValue, hemisphere) {
			// detect changes in tracking mode and hemisphere and reset tracking
			if (uiState.siderealActive) {
				onSiderealClicked(false)
				delay(RESET_TRACKING_DELAY)
				onSiderealClicked(true)
			}
		}

		LazyColumn(
			state = state,
			modifier = modifier,
			contentPadding = PaddingValues(
				top = DimensNormal100,
				bottom = LocalInsets.current.navigationBarInset + DimensNormal200
			),
			verticalArrangement = Arrangement.spacedBy(DimensSmall50)
		) {
			item {
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					IconButton(onClick = { onInfoClick() }) {
						Icon(
							imageVector = ImageVector.vectorResource(R.drawable.ic_information),
							tint = AppTheme.colorScheme.primary,
							contentDescription = null,
							modifier = Modifier
								.size(BigGeneralIconSize)
								.padding(DimensSmall100)
						)
					}

					Text(
						modifier = Modifier
							.weight(1f)
							.padding(vertical = DimensSmall100),
						text = stringResource(id = R.string.main_title).uppercase(),
						style = textStyle20Bold,
						textAlign = TextAlign.Center,
						color = AppTheme.colorScheme.primary,
					)

					IconButton(onClick = { onGearClick() }) {
						Icon(
							imageVector = ImageVector.vectorResource(R.drawable.ic_cog),
							tint = AppTheme.colorScheme.primary,
							contentDescription = null,
							modifier = Modifier
								.size(BigGeneralIconSize)
								.padding(DimensSmall100)
						)
					}
				}
			}

			item {
				ConnectionCard(
					connected = uiState.wifiConnected,
					onCardClick = onConnectionClick,
					haveLocationPermission = uiState.haveLocationPermission,
					haveNotificationPermission = uiState.haveNotificationPermission,
				)
			}

			item {
				Divider(modifier = Modifier.padding(vertical = DimensNormal75))
			}

			item {
				ChecklistCard(
					opened = uiState.openedCheckbox,
					onClick = onChecklistClicked,
					enabled = uiState.wifiConnected && uiState.haveNotificationPermission,
					checkListItems = uiState.checkListItems,
					onCardClick = onChecklistItemClicked,
				)
			}

			item {
				SiderealCard(
					active = uiState.siderealActive,
					onCheckChanged = {
						onSiderealClicked(!uiState.siderealActive)
					},
					enabled = uiState.wifiConnected && uiState.haveNotificationPermission,
					trackingMode = trackingModeValue,
					hemisphere = hemisphere,
				)
			}

			item {
				SlewControlCard(
					slewControlCommands = onSlewControlEvent,
					selectedSpeed = uiState.slewSpeed,
					enabled = uiState.wifiConnected && uiState.haveNotificationPermission,
				)
			}

			item {
				PhotoControlCard(
					uiState = uiState,
					onPhotoControlEvent = onPhotoControlEvent,
					notifyAboutChange = notifyAboutChange,
				)
			}

			item {
				ExpositionTesterCard(
					uiState = uiState,
					onExpositionTesterEvent = onExpositionTesterEvent,
				)
			}
		}
	}
}

@Composable
private fun InitLifecycleListener(onResume: suspend () -> Unit, onStop: suspend () -> Unit) {
	val lifecycleOwner = LocalLifecycleOwner.current
	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			lifecycleOwner.lifecycleScope.launch {
				if (event == Lifecycle.Event.ON_STOP) {
					onStop()
				}

				if (event == Lifecycle.Event.ON_RESUME) {
					onResume()
				}
			}
		}

		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}
}

@Composable
@Preview
internal fun HomeScreenContentPreview() {
	AppTheme {
		DashboardScreenContent(
			uiState = DashboardUiState(),
			onChecklistClicked = {},
			onSiderealClicked = {},
			onSlewControlEvent = {},
			onPhotoControlEvent = {},
			onInfoClick = {},
			onGearClick = {},
			notifyAboutChange = { _, _ -> },
			onConnectionClick = {},
			onChecklistItemClicked = {},
			onExpositionTesterEvent = {}
		)
	}
}
