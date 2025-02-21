package dev.shadoe.delta.hotspot

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NetworkWifi
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.SettingsPower
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiPassword
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.shadoe.delta.hotspot.navigation.LocalNavController
import dev.shadoe.hotspotapi.SoftApEnabledState
import dev.shadoe.hotspotapi.SoftApSecurityType
import dev.shadoe.hotspotapi.SoftApSecurityType.getNameOfSecurityType
import dev.shadoe.hotspotapi.SoftApSecurityType.supportedSecurityTypes
import dev.shadoe.hotspotapi.SoftApSpeedType.getNameOfSpeedType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HotspotEditScreen() {
    val navController = LocalNavController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val hotspotApi = LocalHotspotApiInstance.current!!
    val supportedSpeedTypes = hotspotApi.supportedSpeedTypes.collectAsState()
    val config = hotspotApi.config.collectAsState()

    var mutableConfig by remember(config.value) {
        println("triggering change")
        mutableStateOf(config.value)
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) LargeTopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
        ) {
            item {
                Text(
                    text = "Configure all the values below as per your liking :)",
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = "SSID icon"
                    )
                    OutlinedTextField(
                        value = mutableConfig.ssid ?: "",
                        onValueChange = {
                            mutableConfig = mutableConfig.copy(ssid = it)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        label = { Text("SSID") },
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WifiPassword,
                        contentDescription = "Security Type icon"
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Security Type",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        LazyRow {
                            items(supportedSecurityTypes.size) {
                                FilterChip(
                                    selected = mutableConfig.securityType == supportedSecurityTypes[it],
                                    onClick = {
                                        mutableConfig = mutableConfig.copy(securityType = supportedSecurityTypes[it])
                                    },
                                    label = {
                                        Text(
                                            text = getNameOfSecurityType(
                                                supportedSecurityTypes[it]
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            if (mutableConfig.securityType != SoftApSecurityType.SECURITY_TYPE_OPEN) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Password,
                            contentDescription = "Passphrase icon"
                        )
                        OutlinedTextField(
                            value = mutableConfig.passphrase,
                            onValueChange = {
                                mutableConfig = mutableConfig.copy(
                                    passphrase = it
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            label = { Text("Passphrase") },
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SettingsPower,
                        contentDescription = "Auto Shutdown icon"
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Turn off hotspot automatically?",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "When no devices are connected.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = mutableConfig.isAutoShutdownEnabled,
                        onCheckedChange = {
                            mutableConfig =
                                mutableConfig.copy(isAutoShutdownEnabled = it)
                        },
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NetworkWifi,
                        contentDescription = "Frequency band icon"
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Frequency band",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        LazyRow {
                            items(supportedSpeedTypes.value.size) {
                                FilterChip(
                                    selected = mutableConfig.speedType == supportedSpeedTypes.value[it],
                                    onClick = {
                                        mutableConfig =
                                            mutableConfig.copy(speedType = supportedSpeedTypes.value[it])
                                    },
                                    label = {
                                        Text(
                                            text = getNameOfSpeedType(
                                                supportedSpeedTypes.value[it]
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            item {
                Button(onClick = onClick@{
                    if (mutableConfig.passphrase.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Enter a password.",
                                withDismissAction = true,
                                duration = SnackbarDuration.Short,
                            )
                        }
                        return@onClick
                    }
                    scope.launch {
                        hotspotApi.setSoftApConfiguration(mutableConfig)
                        if (hotspotApi.enabledState.value == SoftApEnabledState.WIFI_AP_STATE_ENABLED) {
                            hotspotApi.stopHotspot()
                            while (hotspotApi.enabledState.value != SoftApEnabledState.WIFI_AP_STATE_DISABLED) {
                                delay(500.milliseconds)
                            }
                            hotspotApi.startHotspot()
                            while (hotspotApi.enabledState.value != SoftApEnabledState.WIFI_AP_STATE_ENABLED) {
                                delay(500.milliseconds)
                            }
                        }
                    }
                        .invokeOnCompletion { if (it == null) navController?.navigateUp() }
                }) {
                    Text(text = "Save")
                }
            }
        }
    }
}