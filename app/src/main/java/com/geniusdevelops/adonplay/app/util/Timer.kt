package com.geniusdevelop.playmyscreens.app.util

import java.util.Timer
import java.util.TimerTask

fun startTimer(intervalMillis: Long, task: () -> Unit): Timer {
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            task()
        }
    }, 0, intervalMillis)
    return timer
}

fun stopTimer(timer: Timer) {
    timer.cancel()
}