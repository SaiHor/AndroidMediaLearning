package com.ngsaihor.medialearning.mdeia.video

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ngsaihor.medialearning.databinding.ActivityMediaTestBinding
import kotlinx.coroutines.launch
import java.io.File


class MediaTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            status.text = "kkk"
        }
        lifecycleScope.launch{
            val rootsd  = Environment.getExternalStorageDirectory()
            val input = File(rootsd?.absolutePath.toString() + "/DCIM/Camera/input.mp4")
            val output = File(rootsd?.absolutePath.toString() + "/test10086/output.mp4")
            if (output.exists()){
                output.delete()
            }
            MediaHandleManager.startMuxer {
                binding.status.text = "${it.presentationTimeUs}"
            }
        }
    }
}