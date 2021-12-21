package com.ngsaihor.medialearning.opengl.util

import android.opengl.Matrix.multiplyMV
import android.opengl.Matrix.setRotateEulerM
import com.ngsaihor.medialearning.opengl.shape.model.Point
import com.ngsaihor.medialearning.opengl.shape.model.Vector
import kotlin.random.Random


class ParticleShooter(
    private val position: Point,
    direction: Vector,
    private val color: Int,
    private val angleVariance: Float = 5f,
    private val speedVariance: Float = 1f
) {

    private val random: Random = Random

    private val rotationMatrix = FloatArray(16)
    private val directionVector = FloatArray(4)
    private val resultVector = FloatArray(4)

    init {
        directionVector[0] = direction.x
        directionVector[1] = direction.y
        directionVector[2] = direction.z
    }

    fun addParticles(particleSystem: ParticleSystem, currentTime: Float, count: Int) {
        for (i in 0 until count) {
            setRotateEulerM(
                rotationMatrix, 0,
                (random.nextFloat() - 0.5f) * angleVariance,
                (random.nextFloat() - 0.5f) * angleVariance,
                (random.nextFloat() - 0.5f) * angleVariance
            )

            multiplyMV(
                resultVector, 0,
                rotationMatrix, 0,
                directionVector, 0
            )

            val speedAdjustment = 1f + random.nextFloat() * speedVariance

            val thisDirection = Vector(
                resultVector[0] * speedAdjustment,
                resultVector[1] * speedAdjustment,
                resultVector[2] * speedAdjustment
            )
            particleSystem.addParticle(position, color, thisDirection, currentTime)
        }
    }

}