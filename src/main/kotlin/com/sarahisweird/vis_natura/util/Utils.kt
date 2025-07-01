package com.sarahisweird.vis_natura.util

fun <T> T.takeIfOrElse(default: T, predicate: (T) -> Boolean): T =
    if (predicate(this)) this else default
