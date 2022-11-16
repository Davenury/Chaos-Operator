package com.github.davenury.lsc_operator.actions

import java.util.*

object Scheduler {

    private val timer = Timer()

    fun schedule(delay: Long, action: () -> Unit) {
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    action()
                }
            },
            delay
        )
    }

}