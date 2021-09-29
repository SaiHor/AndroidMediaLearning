package com.ngsaihor.medialearning.mdeia.audio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ngsaihor.medialearning.databinding.ActivityAudioPlayBinding
import com.ngsaihor.medialearning.formatTime
import com.ngsaihor.medialearning.mdeia.audio.list.AudioAdapter
import com.ngsaihor.medialearning.scanAudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class RecordPlayActivity : AppCompatActivity() {

    private lateinit var adapter: AudioAdapter
    private lateinit var binding: ActivityAudioPlayBinding
    private var isPause = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AudioTrackManager.setStateListener {
            when (it) {
                AudioRecordManager.STATE_PLAYING -> {
                    isPause = false
                    binding.pause.text = "暂停播放"
                }
                AudioRecordManager.STATE_PAUSE -> {
                    isPause = true
                    binding.pause.text = "继续播放"
                }
                AudioRecordManager.STATE_STOP -> {
                    binding.progress.text = "00:00:00"
                }
            }
        }
        AudioTrackManager.setTimerListener {
            binding.progress.text = it.formatTime
        }
        initView()
        initData()
    }

    private fun initView() {
        binding.apply {
            progress.text = "00:00:00"
            pause.setOnClickListener {
                if (isPause) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        AudioTrackManager.resumeMusic()
                    }
                } else {
                    AudioTrackManager.pauseMusic()
                }
            }
            stop.setOnClickListener {
                AudioTrackManager.stopMusic()
            }
        }
        adapter = AudioAdapter(this)
        binding.musicList.layoutManager = LinearLayoutManager(this)
        binding.musicList.adapter = adapter
    }

    private fun initData() {
        val filePath = intent.getStringExtra("filePath") ?: ""
        val file = File(filePath)
        if (file.exists() && file.isFile){
            lifecycleScope.launch(Dispatchers.IO) {
                AudioTrackManager.playPcmByFileName(file.absolutePath)
            }
        }
        adapter.setData(filesDir.scanAudioFile("aac", "pcm"))
        adapter.setOnItemClickListener {
            binding.playingName.text = it.fileName
            lifecycleScope.launch(Dispatchers.IO) {
                AudioTrackManager.playPcmByFileName(it.filePath)
            }
        }
    }
}