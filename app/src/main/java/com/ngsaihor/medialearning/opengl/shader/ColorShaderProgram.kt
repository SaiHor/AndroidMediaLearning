package com.ngsaihor.medialearning.opengl.shader

import android.content.Context
import android.opengl.GLES20
import com.ngsaihor.medialearning.R

class ColorShaderProgram(context: Context) :
    BaseShaderProgram(context, R.raw.ortho_vertext_shader, R.raw.ortho_fragment_shader) {

    private val uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX)
    private val aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)
    private val aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR)

    fun setUniforms(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }

    fun getPositionAttribLocation() = aPositionLocation

    fun getColorAttribLocation() = aColorLocation
}