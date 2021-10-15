package com.ngsaihor.medialearning.opengl.shader

import android.content.Context
import android.opengl.GLES20
import com.ngsaihor.medialearning.R


class ColorShaderProgram(context: Context) :
    BaseShaderProgram(context, R.raw.final_vertext_shader, R.raw.final_fragment_shader) {

    private val uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX)
    private val uColorLocation = GLES20.glGetUniformLocation(program, U_COLOR)
    private val aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)


    fun setUniforms(matrix: FloatArray?, r: Float, g: Float, b: Float) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        GLES20.glUniform4f(uColorLocation, r, g, b, 1f)
    }

    fun getPositionAttribLocation() = aPositionLocation

}