package com.ngsaihor.medialearning.opengl.renderer

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.ngsaihor.medialearning.opengl.shader.ColorShaderProgram
import com.ngsaihor.medialearning.opengl.shader.TextureShaderProgram
import com.ngsaihor.medialearning.opengl.shape.Mallet
import com.ngsaihor.medialearning.opengl.shape.Table
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20.*
import com.ngsaihor.medialearning.R
import com.ngsaihor.medialearning.opengl.loadTexture

class AirHockeyGLRenderer(val context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val table: Table by lazy {
        Table()
    }
    private val mallet: Mallet by lazy {
        Mallet()
    }
    private val textureShaderProgram: TextureShaderProgram by lazy {
        TextureShaderProgram(context)
    }
    private val colorShaderProgram: ColorShaderProgram by lazy {
        ColorShaderProgram(context)
    }

    private var texture = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f,0f,0f,0f)
        texture = loadTexture(context, R.drawable.air_hockey_surface)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Matrix.perspectiveM(projectionMatrix, 0, 45f, width * 1f / height, 1f, 10f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -2.5f)
//        Matrix.rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f)

        val temp = FloatArray(16)
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0)
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.size)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        textureShaderProgram.useProgram()
        textureShaderProgram.setUniforms(projectionMatrix,texture)
        table.bindData(textureShaderProgram)
        table.draw()

        colorShaderProgram.useProgram()
        colorShaderProgram.setUniforms(projectionMatrix)
        mallet.bindData(colorShaderProgram)
        mallet.draw()
    }
}