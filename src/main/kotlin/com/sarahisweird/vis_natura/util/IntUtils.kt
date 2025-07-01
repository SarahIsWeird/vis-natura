package com.sarahisweird.vis_natura.util

typealias Ticks = Int

val Int.seconds: Ticks
    get() = this * 20
