package com.pwd5018.snitch.audit

import com.pwd5018.snitch.data.db.entity.PermissionGrantEntity
import com.pwd5018.snitch.data.db.entity.RiskFlagEntity

/**
 * One rule per concern. Later manifest-based checks (debuggable, allowBackup,
 * exported-without-guard, cleartext traffic, outdated targetSdk) plug in as new
 * implementations of this interface — no restructuring of the audit pipeline needed.
 */
fun interface AuditRule {
    fun evaluate(packageName: String, grants: List<PermissionGrantEntity>, computedAt: Long): RiskFlagEntity?
}
