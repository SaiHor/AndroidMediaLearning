package com.ngsaihor.medialearning.opengl.shader

import android.content.Context
import android.opengl.GLES20
import com.ngsaihor.medialearning.opengl.createProgramByShaderArray

abstract class BaseShaderProgram(context: Context, vertexShaderResId: Int, fragmentShaderResId: Int) {

    protected companion object{
        const val U_MATRIX = "u_Matrix"
        const val U_COLOR = "u_Color"
        const val U_TEXTURE_UNIT = "u_TextureUnit"

        // Attribute constants
        const val A_POSITION = "a_Position"
        const val A_COLOR = "a_Color"
        const val A_TEXTURE_COORDINATES = "a_TextureCoordinates"
    }

    protected val program: Int =
        createProgramByShaderArray(context, listOf(vertexShaderResId), listOf(fragmentShaderResId))


    fun useProgram(){
        GLES20.glUseProgram(program)
    }

}