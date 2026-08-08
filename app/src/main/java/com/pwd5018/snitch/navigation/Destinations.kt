package com.pwd5018.snitch.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.pwd5018.snitch.R

enum class Destination(val route: String, val labelRes: Int, val icon: ImageVector) {
    Audit(route = "audit", labelRes = R.string.nav_audit, icon = Icons.Filled.Security),
    Traffic(route = "traffic", labelRes = R.string.nav_traffic, icon = Icons.Filled.Shield),
}
