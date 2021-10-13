package com.ngsaihor.medialearning.opengl.shape

import android.opengl.GLES20
import com.ngsaihor.medialearning.opengl.shader.BaseShaderProgram
import com.ngsaihor.medialearning.opengl.shader.TextureShaderProgram
import com.ngsaihor.medialearning.opengl.util.VertexArray

class Table : BaseShape() {

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val TEXTURE_COMPONENT_COUNT = 2
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT) * VertexArray.BYTE_PER_FLOAT
    }

    private val tableVertices = floatArrayOf(
        //X,Y,S,T
        0f, 0f, 0.5f, 0.5f,
        -0.5f, -0.8f, 0f, 1f,
        0.5f, -0.8f, 1f, 1f,
        0.5f, 0.8f, 1f, 0f,
        -0.5f, 0.8f, 0f, 0f,
        -0.5f, -0.8f, 0f, 1f,
    )


    private val vertexArray: VertexArray = VertexArray(tableVertices)

    override fun bindData(program: BaseShaderProgram) {
        if (program is TextureShaderProgram) {
            vertexArray.setVertexAttribPointer(
                0,
                program.getPositionAttribLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE
            )

            vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                program.getTextureCoordinateAttribLocation(),
                TEXTURE_COMPONENT_COUNT,
                STRIDE
            )
        }

    }

    override fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)
    }
}