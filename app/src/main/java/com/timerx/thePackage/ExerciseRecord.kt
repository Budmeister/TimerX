package com.timerx.thePackage

import kotlinx.serialization.Serializable

/**
 * Represents a specific instance of exercise with the `title`,
 * the `startTime`, and the `endTime`.
 * @author brian
 */
@Serializable
class ExerciseRecord(var title: String, var startTime: Long, var endTime: Long) : TimeElement {
    override fun toString(): String {
        return "[$title: $startTime,$endTime]"
    }

    override fun startTime(): Long {
        return startTime
    }

    override fun length(): Long {
        return endTime - startTime
    }

    fun mid(): Long {
        return (endTime + startTime) / 2
    }
}