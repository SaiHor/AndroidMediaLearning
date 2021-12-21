package com.ngsaihor.medialearning.opengl.renderer

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix.*
import com.ngsaihor.medialearning.opengl.shader.ParticleShaderProgram
import com.ngsaihor.medialearning.opengl.shape.model.Point
import com.ngsaihor.medialearning.opengl.shape.model.Vector
import com.ngsaihor.medialearning.opengl.util.ParticleShooter
import com.ngsaihor.medialearning.opengl.util.ParticleSystem
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ParticleRenderer(val context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix: FloatArray = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private var globalTime: Long = 0L


    private lateinit var particleProgram: ParticleShaderProgram

    private lateinit var particleSystem: ParticleSystem
    private lateinit var redParticleShooter: ParticleShooter

    private lateinit var greenParticleShooter: ParticleShooter

    private lateinit var blueParticleShooter: ParticleShooter

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        particleProgram = ParticleShaderProgram(context)
        particleSystem = ParticleSystem(3000)
        globalTime = System.nanoTime()
        val particleDirection = Vector(0f, 1f, 0f)
        redParticleShooter = ParticleShooter(
            Point(-1f, 0f, 0f), particleDirection,
            Color.rgb(255, 25, 5)
        )
        greenParticleShooter = ParticleShooter(
            Point(0f, 0f, 0f), particleDirection,
            Color.rgb(25, 255, 25)
        )
        blueParticleShooter = ParticleShooter(
            Point(1f, 0f, 0f), particleDirection,
            Color.rgb(5, 50, 255)
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        perspectiveM(projectionMatrix, 0, 45f, (width.toFloat() / height.toFloat()), 1f, 10f)
        setIdentityM(viewMatrix, 0)
        translateM(viewMatrix, 0, 0f, -1.5f, -5f)
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        val currentTime: Float = (System.nanoTime() - globalTime) / 1000000000f
        redParticleShooter.addParticles(particleSystem, currentTime, 5)
        greenParticleShooter.addParticles(particleSystem, currentTime, 5)
        blueParticleShooter.addParticles(particleSystem, currentTime, 5)
        particleProgram.useProgram()
        particleProgram.setUniforms(viewProjectionMatrix, currentTime)
        particleSystem.bindData(particleProgram)
        particleSystem.draw()
    }
}