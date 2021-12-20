package com.ngsaihor.medialearning.opengl.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.opengl.GLES20.*

class VertexArray(vertexData: FloatArray) {

    companion object {
        const val BYTE_PER_FLOAT = 4
    }

    private val floatBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexData.size * BYTE_PER_FLOAT)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData)

    fun setVertexAttribPointer(
        dataOffset: Int,
        attributeLocation: Int,
        componentCount: Int,
        stride: Int
    ) {
        floatBuffer.position(dataOffset)
        glVertexAttribPointer(
            attributeLocation,
            componentCount,
            GL_FLOAT,
            false,
            stride,
            floatBuffer
        )
        glEnableVertexAttribArray(attributeLocation)
        floatBuffer.position(0)
    }

    fun updateBuffer(vertexData: FloatArray, start: Int, count: Int) {
        floatBuffer.position(start)
        floatBuffer.put(vertexData, start, count)
        floatBuffer.position(0)
    }

}