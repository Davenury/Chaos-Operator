package com.github.davenury.operator.auditlog

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Timestamp

fun Routing.auditLogRouting(auditLog: AuditLog) {
    get("/audit") {
        call.respond(HttpStatusCode.OK, auditLog.getAuditLog().toDto())
    }
}

private data class AuditLogDataDto(
    val actionName: String,
    val scenarioName: String,
    val timestamp: Timestamp
)
private fun List<AuditLogData>.toDto() = this.map {
    AuditLogDataDto(
        it.action.getName(),
        it.scenarioName,
        Timestamp.from(it.timestamp.toInstant())
    )
}
