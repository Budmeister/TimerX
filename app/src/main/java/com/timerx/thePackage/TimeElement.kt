package com.timerx.thePackage

interface TimeElement {

    fun startTime() : Long

    fun startTime(startTime: Long)

    fun length() : Long

    fun length(length: Long)

    fun copy() : TimeElement

    fun name() : String

}