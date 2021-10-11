package com.ngsaihor.medialearning.opengl.shape

import android.content.Context
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.opengl.GLES20.*
import com.ngsaihor.medialearning.R
import com.ngsaihor.medialearning.opengl.createProgramByShaderArray

class AirHockey(var context: Context) {

    private val tableVertices = floatArrayOf(
        -0.5f, -0.5f,
        0.5f, 0.5f,
        -0.5f, 0.5f,

        // Triangle 2
        -0.5f, -0.5f,
        0.5f, -0.5f,
        0.5f, 0.5f,

        // Line 1
        -0.5f, 0f,
        0.5f, 0f,

        // Mallets
        0f, -0.25f,
        0f, 0.25f
    )


    private var vertexBuffer: FloatBuffer
    private var mProgram = 0
    private var uColorLocation = 0
    private var aPositionLocation = 0

    init {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        vertexBuffer = ByteBuffer.allocateDirect(tableVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(tableVertices)

        mProgram = createProgramByShaderArray(
            context,
            listOf(R.raw.simple_vertex_shader),
            listOf(R.raw.simple_fragment_shader)
        )
        glUseProgram(mProgram)

        uColorLocation = glGetUniformLocation(mProgram, U_COLOR)
        aPositionLocation = glGetAttribLocation(mProgram, A_POSITION)

        vertexBuffer.position(0)
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 0, vertexBuffer)
        glEnableVertexAttribArray(aPositionLocation)
    }


    companion object {
        private const val U_COLOR = "u_color"
        private const val A_POSITION = "a_position"
    }


    fun draw() {
        glClear(GL_COLOR_BUFFER_BIT)

        // Draw the table.
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        // Draw the center dividing line.
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        glDrawArrays(GL_LINES, 6, 2)

        // Draw the first mallet blue.
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f)
        glDrawArrays(GL_POINTS, 8, 1)

        // Draw the second mallet red.
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        glDrawArrays(GL_POINTS, 9, 1)
    }
}