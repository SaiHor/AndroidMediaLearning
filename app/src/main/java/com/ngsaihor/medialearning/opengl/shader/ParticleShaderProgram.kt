package com.ngsaihor.medialearning.opengl.shader

import android.content.Context
import android.opengl.GLES20
import com.ngsaihor.medialearning.R

class ParticleShaderProgram(context: Context) :
    BaseShaderProgram(context, R.raw.particle_vertex_shader, R.raw.particle_fragment_shader) {

    // Uniform locations
    private val uMatrixLocation: Int by lazy {
        GLES20.glGetUniformLocation(program, U_MATRIX)
    }

    private val uTimeLocation: Int by lazy {
        GLES20.glGetUniformLocation(program, U_TIME)
    }

    private val uTextureUnitLocation: Int by lazy {
        GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT)
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

    fun setUniforms(matrix: FloatArray?, elapsedTime: Float, textureId: Int) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        GLES20.glUniform1f(uTimeLocation, elapsedTime)

        GLES20.glActiveTexture(textureId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1f(uTextureUnitLocation, 0f)
    }


}