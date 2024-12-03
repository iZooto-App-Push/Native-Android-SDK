package com.izooto.feature.pulseweb

object PulseManager {
    val instance: PWInterface by lazy { PulseHandler() }
}