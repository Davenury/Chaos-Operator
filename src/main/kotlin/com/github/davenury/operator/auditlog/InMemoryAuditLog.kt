package com.github.davenury.operator.auditlog

import com.github.davenury.operator.actions.Action
import java.time.Clock
import java.time.ZonedDateTime

class InMemoryAuditLog(
    private val clock: Clock
): AuditLog {
    private val auditLog = mutableListOf<AuditLogData>()

    override fun logAction(action: Action, scenarioName: String) {
        auditLog.add(AuditLogData(action, scenarioName, ZonedDateTime.now(clock)))
    }

    override fun getAuditLog(): List<AuditLogData> = auditLog
}