package com.ngsaihor.medialearning.opengl.shader

import android.content.Context
import android.opengl.GLES20
import com.ngsaihor.medialearning.R

class ParticleShaderProgram(context: Context) :
    BaseShaderProgram(context, R.raw.particle_vertex_shader, R.raw.particle_fragment_shader) {

    // Uniform locations
    val uMatrixLocation: Int by lazy {
        GLES20.glGetUniformLocation(program, U_MATRIX)
    }

    val uTimeLocation: Int by lazy {
        GLES20.glGetUniformLocation(program, U_TIME)
    }

    // Attribute locations
    val aPositionLocation: Int by lazy {
        GLES20.glGetAttribLocation(program, A_POSITION)
    }
    val aColorLocation: Int by lazy {
        GLES20.glGetAttribLocation(program, A_COLOR)
    }
    val aDirectionVectorLocation: Int by lazy {
        GLES20.glGetAttribLocation(program, A_DIRECTION_VECTOR)
    }

    val aParticleStartTimeLocation: Int by lazy {
        GLES20.glGetAttribLocation(program, A_PARTICLE_START_TIME)
    }

    fun setUniforms(matrix: FloatArray?, elapsedTime: Float) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        GLES20.glUniform1f(uTimeLocation, elapsedTime)
    }


}