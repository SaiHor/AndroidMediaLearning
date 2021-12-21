package com.ngsaihor.medialearning.opengl.shader

import android.content.Context
import android.opengl.GLES20
import com.ngsaihor.medialearning.R

class FrameAnimShaderProgram(context: Context) :
    BaseShaderProgram(context, R.raw.frame_anim_vertext_shader, R.raw.frame_anim_fragment_shader) {

    private val uTextureLocation = GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT)

    val aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)

    val aTextureCoordinateLocation = GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES)

    fun setUniforms(textureId: Int) {

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glUniform1i(uTextureLocation, 0)
    }
}