package com.github.davenury.operator

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import meterRegistry

fun Routing.metaRouting() {

    get("/health") {
        call.respondText("All Good!")
    }

    get("/metrics") {
        call.respond(meterRegistry.scrape())
    }

}