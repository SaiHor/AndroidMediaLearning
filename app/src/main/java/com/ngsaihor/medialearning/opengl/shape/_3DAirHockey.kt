package com.ngsaihor.medialearning.opengl.shape

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.ngsaihor.medialearning.R
import com.ngsaihor.medialearning.opengl.createProgramByShaderArray
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class _3DAirHockey(var context: Context) {

    companion object {
        private const val U_MATRIX = "u_Matrix"
        private const val A_COLOR = "a_Color"
        private const val A_POSITION = "a_Position"
        private const val POSITION_COMPONENT_COUNT = 2
        private const val COLOR_COMPONENT_COUNT = 3
        private const val BYTES_PER_FLOAT = 4

        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val tableVertices = floatArrayOf(
        //X,Y,R,G,B
        0f, 0f, 1f, 1f, 1f,
        -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
        0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
        0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
        -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
        -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,

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
    private var uMatrixLocation = 0

    init {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        vertexBuffer = ByteBuffer.allocateDirect(tableVertices.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(tableVertices)

        mProgram = createProgramByShaderArray(
            context,
            listOf(R.raw._3d_vertext_shader),
            listOf(R.raw._3d_fragment_shader)
        )
        GLES20.glUseProgram(mProgram)

        aColorLocation = GLES20.glGetAttribLocation(mProgram, A_COLOR)
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION)
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX)

        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT,
            GLES20.GL_FLOAT,
            false,
            STRIDE,
            vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(aPositionLocation)

        vertexBuffer.position(POSITION_COMPONENT_COUNT)
        GLES20.glVertexAttribPointer(
            aColorLocation,
            POSITION_COMPONENT_COUNT,
            GLES20.GL_FLOAT,
            false,
            STRIDE,
            vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(aColorLocation)
    }

    fun sizeChange(width: Int, height: Int) {
        Matrix.perspectiveM(projectionMatrix, 0, 45f, width * 1f / height, 1f, 10f)
        Matrix.setIdentityM(modelMatrix,0)
        Matrix.translateM(modelMatrix,0,0f,0f,-3f)
        Matrix.rotateM(modelMatrix,0,-60f,1f,0f,0f)

        val temp = FloatArray(16)
        Matrix.multiplyMM(temp,0,projectionMatrix,0,modelMatrix,0)
        System.arraycopy(temp,0,projectionMatrix,0,temp.size)


    }

    fun draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)


        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2)


        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1)


        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1)
    }
}