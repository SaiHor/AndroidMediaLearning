package com.ngsaihor.medialearning.opengl.shape

import android.opengl.GLES20
import com.ngsaihor.medialearning.opengl.shader.BaseShaderProgram
import com.ngsaihor.medialearning.opengl.shader.ColorShaderProgram
import com.ngsaihor.medialearning.opengl.util.VertexArray

class Mallet : BaseShape() {

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val COLOR_COMPONENT_COUNT = 3
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * VertexArray.BYTE_PER_FLOAT
    }

    private val vertexData = floatArrayOf(
        //X,Y,R,G,B
        // Mallets
        0f, -0.4f, 0f, 0f, 1f,
        0f, 0.5f, 1f, 0f, 0f
    )

    private val vertexArray = VertexArray(vertexData)

    override fun bindData(program: BaseShaderProgram) {
        if (program is ColorShaderProgram) {
            vertexArray.setVertexAttribPointer(
                0,
                program.getPositionAttribLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE
            )

            vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                program.getColorAttribLocation(),
                COLOR_COMPONENT_COUNT,
                STRIDE
            )
        }
    }

    override fun draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 2)
    }
}