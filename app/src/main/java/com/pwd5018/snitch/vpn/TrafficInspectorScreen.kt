package com.pwd5018.snitch.vpn

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pwd5018.snitch.SnitchApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrafficInspectorScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as SnitchApplication
    val viewModel: VpnViewModel = viewModel(factory = VpnViewModel.Factory(app.container.vpnStatusRepository))
    val state by viewModel.state.collectAsStateWithLifecycle()

    val vpnConsentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.connect()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        // Notification just won't show if denied; the tunnel still works either way.
    }

    fun startVpn() {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        val consentIntent = VpnPermissionHelper.prepareIntent(context)
        if (consentIntent != null) {
            vpnConsentLauncher.launch(consentIntent)
        } else {
            viewModel.connect()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Traffic Inspector") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = when (val current = state) {
                    VpnState.Stopped -> "Stopped"
                    VpnState.Starting -> "Starting…"
                    VpnState.Running -> "Running (test subnet only — round 1 skeleton)"
                    is VpnState.Error -> "Error: ${current.message}"
                },
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                "This round only proves the tunnel lifecycle works. No traffic is proxied, " +
                    "decrypted, or forwarded yet — the VPN claims an unused test subnet, not " +
                    "your real network traffic.",
                style = MaterialTheme.typography.bodyMedium,
            )
            when (state) {
                VpnState.Stopped, is VpnState.Error -> {
                    Button(onClick = ::startVpn) { Text("Start tunnel") }
                }
                VpnState.Running -> {
                    Button(onClick = viewModel::disconnect) { Text("Stop tunnel") }
                }
                VpnState.Starting -> {
                    Button(onClick = {}, enabled = false) { Text("Starting…") }
                }
            }
        }
    }
}
