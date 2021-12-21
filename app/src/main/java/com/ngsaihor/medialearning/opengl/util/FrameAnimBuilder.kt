package com.ngsaihor.medialearning.opengl.util

import android.opengl.GLES20
import com.ngsaihor.medialearning.opengl.shader.FrameAnimShaderProgram

class FrameAnimBuilder {

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val TEXTURE_COMPONENT_COUNT = 2
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT) * VertexArray.BYTE_PER_FLOAT
    }

    private val tableVertices = floatArrayOf(
        //X,Y,S,T
        0f, 0f, 0.5f, 0.5f,
        -1f, -1f, 0f, 1f,
        1f, -1f, 1f, 1f,
        1f, 1f, 1f, 0f,
        -1f, 1f, 0f, 0f,
        -1f, -1f, 0f, 1f,
    )


    private val vertexArray: VertexArray = VertexArray(tableVertices)

    fun bindData(program: FrameAnimShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            program.aPositionLocation,
            POSITION_COMPONENT_COUNT,
            STRIDE
        )

        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            program.aTextureCoordinateLocation,
            TEXTURE_COMPONENT_COUNT,
            STRIDE
        )

    }

    fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)
    }
}