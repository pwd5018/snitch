package com.pwd5018.snitch.audit

import com.pwd5018.snitch.data.db.entity.RiskFlagEntity
import com.pwd5018.snitch.data.db.entity.RiskFlagType
import com.pwd5018.snitch.data.db.entity.RiskSeverity

/**
 * Round-1 rule set. The "no obvious purpose" framing for mic/camera is a deliberately naive
 * heuristic — it flags any granted mic/camera permission generically, not a real
 * purpose-inference (that would need cross-referencing declared use, foreground-only access,
 * etc.). Refine later if this proves too noisy in practice.
 */
object RiskRules {

    private fun grantedRule(
        permissionName: String,
        flagType: String,
        severity: String,
        detail: String,
    ) = AuditRule { packageName, grants, computedAt ->
        val isGranted = grants.any { it.permissionName == permissionName && it.isGranted }
        if (!isGranted) {
            null
        } else {
            RiskFlagEntity(
                packageName = packageName,
                flagType = flagType,
                severity = severity,
                detail = detail,
                computedAt = computedAt,
            )
        }
    }

    val backgroundLocation = grantedRule(
        permissionName = "android.permission.ACCESS_BACKGROUND_LOCATION",
        flagType = RiskFlagType.BACKGROUND_LOCATION,
        severity = RiskSeverity.HIGH,
        detail = "Can access location even when the app isn't in use.",
    )

    val microphone = grantedRule(
        permissionName = "android.permission.RECORD_AUDIO",
        flagType = RiskFlagType.MIC_NO_PURPOSE,
        severity = RiskSeverity.MEDIUM,
        detail = "Holds microphone access — verify this matches the app's purpose.",
    )

    val camera = grantedRule(
        permissionName = "android.permission.CAMERA",
        flagType = RiskFlagType.CAMERA_NO_PURPOSE,
        severity = RiskSeverity.MEDIUM,
        detail = "Holds camera access — verify this matches the app's purpose.",
    )

    val smsRead = grantedRule(
        permissionName = "android.permission.READ_SMS",
        flagType = RiskFlagType.SMS_READ,
        severity = RiskSeverity.HIGH,
        detail = "Can read SMS messages.",
    )

    val contactsRead = grantedRule(
        permissionName = "android.permission.READ_CONTACTS",
        flagType = RiskFlagType.CONTACTS_READ,
        severity = RiskSeverity.MEDIUM,
        detail = "Can read your contacts.",
    )

    val all: List<AuditRule> = listOf(backgroundLocation, microphone, camera, smsRead, contactsRead)
}
