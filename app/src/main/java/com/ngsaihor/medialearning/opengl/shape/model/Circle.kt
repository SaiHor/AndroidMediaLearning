package com.ngsaihor.medialearning.opengl.shape.model

data class Circle(val centerPoint: Point, val radius: Float) {
    fun scale(scale: Float) = Circle(centerPoint, scale * radius)
}
