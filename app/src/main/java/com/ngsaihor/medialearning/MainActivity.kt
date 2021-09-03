package com.ngsaihor.medialearning

import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ngsaihor.medialearning.databinding.ActivityMainBinding
import com.ngsaihor.medialearning.mdeia.audio.AudioEncodeManager
import com.ngsaihor.medialearning.mdeia.audio.AudioTrackManager
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback2 {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch{
            val rootsd  = Environment.getExternalStorageDirectory()
            val m4a = File(rootsd?.absolutePath.toString() + "/test10086/aac_input.m4a")
//            AudioCodecManager.playAAC(m4a.absolutePath)
//            AudioTrackManager.playPcmByFileName(rootsd?.absolutePath.toString() + "/test10086/pcm_output.pcm",this@MainActivity)


            AudioEncodeManager.encodeAction(rootsd?.absolutePath.toString() + "/test10086/pcm_output.pcm",rootsd?.absolutePath.toString() + "/test10086/aac_output.aac")
        }




        //          SurfaceView(this).apply {
//              setContentView(this)
//              holder.addCallback(this@MainActivity)
//          }

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val rootsd  = Environment.getExternalStorageDirectory()
        val input = File(rootsd?.absolutePath.toString() + "/DCIM/Camera/input.mp4")
        lifecycleScope.launch{
//            VideoCodecManager.playVideoByFilePathWithSurface(input.absolutePath,holder.surface)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {

    }
}