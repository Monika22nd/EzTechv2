package com.eztech.core.common

fun String?.orDash(): String = takeUnless { it.isNullOrBlank() } ?: "-"

