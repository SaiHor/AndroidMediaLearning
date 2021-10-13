package com.ngsaihor.medialearning.opengl.shape.simple_shape

import android.content.Context
import android.opengl.GLES20.*
import com.ngsaihor.medialearning.R
import com.ngsaihor.medialearning.opengl.createProgramByShaderArray
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SmoothColorAirHockey(var context: Context) {

    companion object {
        private const val A_COLOR = "a_Color"
        private const val A_POSITION = "a_Position"
        private const val POSITION_COMPONENT_COUNT = 2
        private const val COLOR_COMPONENT_COUNT = 3
        private const val BYTES_PER_FLOAT = 4

        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }

    private val tableVertices = floatArrayOf(
        //X,Y,R,G,B
        0f, 0f, 1f, 1f, 1f,
        -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
        0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
        0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
        -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
        -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,

        // Line 1
        -0.5f, 0f, 1f, 0f, 0f,
        0.5f, 0f, 1f, 0f, 0f,

        // Mallets
        0f, -0.25f, 0f, 0f, 1f,
        0f, 0.25f, 1f, 0f, 0f
    )


    private var vertexBuffer: FloatBuffer
    private var mProgram = 0
    private var aColorLocation = 0
    private var aPositionLocation = 0

    init {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        vertexBuffer = ByteBuffer.allocateDirect(tableVertices.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(tableVertices)

        mProgram = createProgramByShaderArray(
            context,
            listOf(R.raw.smooth_vertext_shader),
            listOf(R.raw.smooth_fragment_shader)
        )
        glUseProgram(mProgram)

        aColorLocation = glGetAttribLocation(mProgram, A_COLOR)
        aPositionLocation = glGetAttribLocation(mProgram, A_POSITION)

        vertexBuffer.position(0)
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexBuffer)
        glEnableVertexAttribArray(aPositionLocation)

        vertexBuffer.position(POSITION_COMPONENT_COUNT)
        glVertexAttribPointer(aColorLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexBuffer)
        glEnableVertexAttribArray(aColorLocation)
    }



    fun draw() {
        glClear(GL_COLOR_BUFFER_BIT)


        glDrawArrays(GL_TRIANGLE_FAN, 0, 6)


        glDrawArrays(GL_LINES, 6, 2)


        glDrawArrays(GL_POINTS, 8, 1)


        glDrawArrays(GL_POINTS, 9, 1)
    }
}