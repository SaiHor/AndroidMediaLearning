package com.ngsaihor.medialearning.opengl.shape.model

data class Point(val x: Float, val y: Float, val z: Float) {
    fun translateY(distance: Float) = Point(x, y + distance, z)
}