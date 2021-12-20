package com.ngsaihor.medialearning.opengl

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ngsaihor.medialearning.opengl.renderer.AirHockeyGLRenderer
import com.ngsaihor.medialearning.opengl.renderer.ParticleRenderer

class OpenGLTestActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }


    private inner class MyGLSurfaceView(context: Activity) : GLSurfaceView(context) {
        private val mGLRenderer = ParticleRenderer(context)

        init {
            setEGLContextClientVersion(2)
            setRenderer(mGLRenderer)
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }


}