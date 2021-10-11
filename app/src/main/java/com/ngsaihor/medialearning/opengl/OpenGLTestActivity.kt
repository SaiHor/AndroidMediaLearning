package com.ngsaihor.medialearning.opengl

import android.app.Activity
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ngsaihor.medialearning.opengl.shape.AirHockey
import com.ngsaihor.medialearning.opengl.shape.SmoothColorAirHockey
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLTestActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }


    private inner class MyGLSurfaceView(context: Activity) : GLSurfaceView(context) {
        private val mGLRenderer = MyGLRenderer()

        init {
            setEGLContextClientVersion(2)
            setRenderer(mGLRenderer)
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }

    private inner class MyGLRenderer : GLSurfaceView.Renderer {

        private lateinit var airHockey:AirHockey

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(1f,1f,1f,1f)
            airHockey = AirHockey(this@OpenGLTestActivity)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            airHockey.draw()
        }

    }
}