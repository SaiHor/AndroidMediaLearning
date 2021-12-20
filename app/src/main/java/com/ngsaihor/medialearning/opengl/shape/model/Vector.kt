package com.ngsaihor.medialearning.opengl.shape.model


class Vector(val x: Float, val y: Float, val z: Float) {
    fun length(): Float {
        return Math.sqrt(
            (x * x + y * y + z * z).toDouble()
        ).toFloat()
    }


    fun crossProduct(other: Vector): Vector {
        return Vector(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun dotProduct(other: Vector): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun scale(f: Float): Vector {
        return Vector(
            x * f,
            y * f,
            z * f
        )
    }
}