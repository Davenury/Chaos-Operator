package com.github.davenury.operator.auditlog

import com.github.davenury.operator.actions.Action
import java.time.ZonedDateTime

interface AuditLog {
    fun logAction(action: Action, scenarioName: String)
    fun getAuditLog(): List<AuditLogData>
}

data class AuditLogData(
    val action: Action,
    val scenarioName: String,
    val timestamp: ZonedDateTime
)