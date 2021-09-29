package com.ngsaihor.medialearning

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ngsaihor.medialearning.databinding.ActivityMainBinding
import com.ngsaihor.medialearning.mdeia.audio.RecordPlayActivity
import com.ngsaihor.medialearning.mdeia.audio.AudioRecordActivity


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toRecord.setOnClickListener {
                startActivity(Intent(this@MainActivity, AudioRecordActivity::class.java))
            }
            toRecordPlay.setOnClickListener {
                startActivity(Intent(this@MainActivity, RecordPlayActivity::class.java))
            }
        }
    }


}