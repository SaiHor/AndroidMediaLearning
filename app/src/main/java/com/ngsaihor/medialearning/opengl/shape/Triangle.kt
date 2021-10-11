package com.ngsaihor.medialearning.opengl.shape

import android.opengl.GLES20
import com.ngsaihor.medialearning.opengl.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Triangle {
    companion object {
        const val COORDINATES_PRE_VERTEX = 3
    }

    /**
     * 顶点着色器代码
     * attribute变量(属性变量)只能用于顶点着色器中
     * uniforms变量(一致变量)用来将数据值从应用程其序传递到顶点着色器或者片元着色器。 。
     * varying变量(易变变量)是从顶点着色器传递到片元着色器的数据变量。
     * gl_Position （必须）为内建变量，表示变换后点的空间位置。
     */
    private val vertexShaderCode = "attribute vec4 vPosition;" +  // 应用程序传入顶点着色器的顶点位置
            "void main() {" +
            "  gl_Position = vPosition;" +  // 设置此次绘制此顶点位置
            "}"

    /**
     * 片元着色器代码
     */
    private val fragmentShaderCode = "precision mediump float;" +  // 设置工作精度
            "uniform vec4 vColor;" +  // 应用程序传入着色器的颜色变量
            "void main() {" +
            "  gl_FragColor = vColor;" +  // 颜色值传给 gl_FragColor内建变量，完成片元的着色
            "}"


    val triangleCoords = floatArrayOf(
        0.0f, 1.0f, 0.0f,  // top 屏幕顶端中心点
        -1.0f, -1.0f, 0.0f,  // bottom left 屏幕底部左下角
        1.0f, -1.0f, 0.0f // bottom right 屏幕底部右下角
        //以上坐标z都为0 创建一个平面的三角形
    )

    private var vertexBuffer: FloatBuffer
    private var mProgram = 0

    var color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    init {
        val byteBuffer = ByteBuffer.allocateDirect(
            // 顶点坐标个数 * 坐标数据类型 float 一个是 4 bytes
            triangleCoords.size * 4
        )
        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()
        vertexBuffer.put(triangleCoords)
        vertexBuffer.position(0)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)
    }

    private var mPositionHandle //变量 用于存取attribute修饰的变量的位置编号
            = 0
    private var mColorHandle //变量 用于存取uniform修饰的变量的位置编号
            = 0
    private val vertexStride: Int = COORDINATES_PRE_VERTEX * 4

    private val vertexCount: Int = triangleCoords.size / COORDINATES_PRE_VERTEX

    fun draw() {
        GLES20.glUseProgram(mProgram)
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glVertexAttribPointer(
            mPositionHandle, COORDINATES_PRE_VERTEX,
            GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        GLES20.glDisableVertexAttribArray(mPositionHandle)

    }

}