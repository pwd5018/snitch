package com.pwd5018.snitch.audit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pwd5018.snitch.SnitchApplication
import com.pwd5018.snitch.audit.components.AppRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen() {
    val app = LocalContext.current.applicationContext as SnitchApplication
    val viewModel: AuditViewModel = viewModel(factory = AuditViewModel.Factory(app.container.auditRepository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var expandedPackage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Privacy & Security Audit") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::rescan) {
                Icon(Icons.Filled.Refresh, contentDescription = "Rescan")
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.isScanning && uiState.apps.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Text("Scanning installed apps…", modifier = Modifier.padding(top = 12.dp))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Text(
                            "${uiState.apps.size} apps scanned" +
                                if (uiState.isScanning) " (rescanning…)" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(uiState.apps, key = { it.app.packageName }) { entry ->
                        AppRow(
                            entry = entry,
                            expanded = expandedPackage == entry.app.packageName,
                            onToggleExpanded = {
                                expandedPackage = if (expandedPackage == entry.app.packageName) {
                                    null
                                } else {
                                    entry.app.packageName
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
