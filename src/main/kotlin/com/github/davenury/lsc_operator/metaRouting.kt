package com.github.davenury.lsc_operator

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.metaRouting() {

    get("/health") {
        call.respondText("All Good!")
    }

}