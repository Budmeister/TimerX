package com.timerx.thePackage

import kotlinx.serialization.Serializable

@Serializable
class Settings(
    var exerciseNames: MutableList<String>,
    var exerciseColors: MutableList<Int>,
    var currentRecord: ExerciseRecord?
){
    override fun toString(): String {
        return "[exerciseNames=$exerciseNames, exerciseColors=$exerciseColors, currentRecord:$currentRecord]"
    }
}