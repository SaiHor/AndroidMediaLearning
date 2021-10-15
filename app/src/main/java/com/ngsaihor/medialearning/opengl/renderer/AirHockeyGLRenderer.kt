package com.ngsaihor.medialearning.opengl.renderer

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.ngsaihor.medialearning.R
import com.ngsaihor.medialearning.opengl.loadTexture
import com.ngsaihor.medialearning.opengl.shader.ColorShaderProgram
import com.ngsaihor.medialearning.opengl.shader.TextureShaderProgram
import com.ngsaihor.medialearning.opengl.shape.Mallet
import com.ngsaihor.medialearning.opengl.shape.Puck
import com.ngsaihor.medialearning.opengl.shape.Table
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class AirHockeyGLRenderer(val context: Context) : GLSurfaceView.Renderer {

    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val table: Table by lazy {
        Table()
    }
    private val mallet: Mallet by lazy {
        Mallet(0.08f, 0.15f, 32)
    }
    private val puck: Puck by lazy {
        Puck(0.06f, 0.02f, 32)
    }

    private val textureShaderProgram: TextureShaderProgram by lazy {
        TextureShaderProgram(context)
    }
    private val colorShaderProgram: ColorShaderProgram by lazy {
        ColorShaderProgram(context)
    }

    private var texture = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 0f)
        texture = loadTexture(context, R.drawable.air_hockey_surface)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        Matrix.perspectiveM(
            projectionMatrix, 0,45f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )
        Matrix.setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        // Multiply the view and projection matrices together.
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Draw the table.
        positionTableInScene()
        textureShaderProgram.useProgram()
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, texture)
        table.bindData(textureShaderProgram)
        table.draw()

        // Draw the mallets.
        positionObjectInScene(0f, mallet.height / 2f, -0.4f)
        colorShaderProgram.useProgram()
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f)
        mallet.bindData(colorShaderProgram)
        mallet.draw()

        positionObjectInScene(0f, mallet.height / 2f, 0.4f)
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f)
        // Note that we don't have to define the object data twice -- we just
        // draw the same mallet again but in a different position and with a
        // different color.
        mallet.draw()

        // Draw the puck.
//        positionObjectInScene(0f, puck.height / 2f, 0f)
//        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f)
//        puck.bindData(colorShaderProgram)
//        puck.draw()

    }

    private fun positionTableInScene() {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
        Matrix.multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
    }

    // The mallets and the puck are positioned on the same plane as the table.
    private fun positionObjectInScene(x: Float, y: Float, z: Float) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
    }
}