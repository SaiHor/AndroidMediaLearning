package com.ngsaihor.medialearning.opengl.shader

import android.content.Context
import android.opengl.GLES20
import com.ngsaihor.medialearning.R

class TextureShaderProgram(context: Context) :
    BaseShaderProgram(context, R.raw.texture_vertext_shader, R.raw.texture_fragment_shader) {

    private val uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX)
    private val uTextureLocation = GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT)

    private val aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)

    private val aTextureCoordinateLocation = GLES20.glGetAttribLocation(
        program,
        A_TEXTURE_COORDINATES
    )

    fun setUniforms(matrix: FloatArray, textureId: Int) {
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId)

        GLES20.glUniform1i(uTextureLocation,0)
    }

    fun getPositionAttribLocation() = aPositionLocation

    fun getTextureCoordinateAttribLocation() = aTextureCoordinateLocation
}