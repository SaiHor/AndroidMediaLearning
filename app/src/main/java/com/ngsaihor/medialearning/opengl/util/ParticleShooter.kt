package com.ngsaihor.medialearning.opengl.util

import com.ngsaihor.medialearning.opengl.shape.model.Point
import com.ngsaihor.medialearning.opengl.shape.model.Vector

class ParticleShooter(
    private val position: Point,
    private val direction: Vector,
    private val color: Int
) {
    fun addParticles(particleSystem: ParticleSystem, currentTime: Float, count: Int) {
        for (i in 0 until count) {
            particleSystem.addParticle(position, color, direction, currentTime)
        }
    }

}