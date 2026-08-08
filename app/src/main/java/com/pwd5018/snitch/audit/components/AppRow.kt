package com.pwd5018.snitch.audit.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pwd5018.snitch.data.db.entity.RiskSeverity
import com.pwd5018.snitch.data.db.relation.AppWithGrantsAndFlags
import com.pwd5018.snitch.ui.theme.RiskHigh
import com.pwd5018.snitch.ui.theme.RiskLow
import com.pwd5018.snitch.ui.theme.RiskMedium

private fun highestSeverityColor(entry: AppWithGrantsAndFlags): Color? {
    val severities = entry.flags.map { it.severity }
    return when {
        RiskSeverity.HIGH in severities -> RiskHigh
        RiskSeverity.MEDIUM in severities -> RiskMedium
        RiskSeverity.LOW in severities -> RiskLow
        else -> null
    }
}

@Composable
fun AppRow(
    entry: AppWithGrantsAndFlags,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpanded),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.app.appLabel, style = MaterialTheme.typography.titleMedium)
                    Text(
                        entry.app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val badgeColor = highestSeverityColor(entry)
                if (badgeColor != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SeverityDot(color = badgeColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${entry.flags.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeColor,
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider()
                    Text(
                        "Permissions",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                    entry.grants.sortedByDescending { it.isGranted }.forEach { grant ->
                        Text(
                            "${if (grant.isGranted) "✓" else "✗"} ${grant.permissionName.substringAfterLast('.')} (${grant.protectionLevel})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (grant.isGranted) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    if (entry.flags.isNotEmpty()) {
                        Text(
                            "Flags",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        )
                        entry.flags.forEach { flag ->
                            Text(
                                "${flag.severity}: ${flag.detail}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color),
    )
}
