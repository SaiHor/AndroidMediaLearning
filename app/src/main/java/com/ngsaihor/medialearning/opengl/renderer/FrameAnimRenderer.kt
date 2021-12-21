package com.ngsaihor.medialearning.opengl.renderer

import android.content.Context
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.ngsaihor.medialearning.R
import com.ngsaihor.medialearning.opengl.loadTexture
import com.ngsaihor.medialearning.opengl.shader.FrameAnimShaderProgram
import com.ngsaihor.medialearning.opengl.util.FrameAnimBuilder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class FrameAnimRenderer(val context: Context) : GLSurfaceView.Renderer {

    private val frameAnimShaderProgram: FrameAnimShaderProgram by lazy {
        FrameAnimShaderProgram(context)
    }

    private val textureIdArray by lazy {
        initTextureIdArray()
    }

    private fun initTextureIdArray():IntArray{
        val textureIdArray = IntArray(32)
        for (i in 0..31){
            textureIdArray[i] = loadTexture(context, getImageResourceId("carrot_$i"))
        }
        return textureIdArray
    }

    private val builder: FrameAnimBuilder by lazy {
        FrameAnimBuilder()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    var currentPos = 0

    override fun onDrawFrame(gl: GL10?) {
        frameAnimShaderProgram.useProgram()
        frameAnimShaderProgram.setUniforms(textureIdArray[currentPos])
        currentPos++
        if (currentPos > 31) {
            currentPos = 0
        }
        builder.bindData(frameAnimShaderProgram)
        builder.draw()
    }


    private fun getImageResourceId(name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

}