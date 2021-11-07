package com.timerx.thePackage

import kotlinx.serialization.Serializable

/**
 * Holds the settings of the app, namely, the exercise names, their colors,
 * and the record currently being recorded if any.
 * @author Brian Smith
 */
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