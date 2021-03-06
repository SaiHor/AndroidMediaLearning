package com.ngsaihor.medialearning.mdeia.audio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ngsaihor.medialearning.databinding.ActivityAudioRecordBinding
import com.ngsaihor.medialearning.formatTime
import com.ngsaihor.medialearning.mdeia.audio.list.AudioAdapter
import com.ngsaihor.medialearning.scanAudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioRecordBinding
    private lateinit var adapter: AudioAdapter
    private var isPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AudioRecordManager.setFilePathAndName(filesDir.absolutePath)
        AudioRecordManager.setStateListener {
            when (it) {
                AudioRecordManager.STATE_PLAYING -> {
                    isPause = false
                    binding.start.isEnabled = false
                    binding.pause.isVisible = true
                    binding.stop.isVisible = true
                    binding.pause.text = "暂停录音"
                }
                AudioRecordManager.STATE_PAUSE -> {
                    isPause = true
                    binding.pause.text = "继续录音"
                }
                AudioRecordManager.STATE_STOP -> {
                    binding.progress.text = "00:00:00"
                    binding.start.isEnabled = true
                    binding.pause.isVisible = false
                    binding.stop.isVisible = false
                }
            }
        }
        AudioRecordManager.setTimerListener {
            binding.progress.text = it.formatTime
        }
        initView()
        initData()
    }

    private fun initView() {
        binding.apply {
            progress.text = "00:00:00"
            start.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    AudioRecordManager.startRecord(isPause)
                }
            }
            pause.setOnClickListener {
                if (isPause) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        AudioRecordManager.startRecord(true)
                    }
                } else {
                    AudioRecordManager.pause()
                }
            }
            stop.setOnClickListener {
                AudioRecordManager.stop()
                binding.root.postDelayed({
                    adapter.setData(filesDir.scanAudioFile("aac","pcm"))
                }, 100)
            }
        }
        adapter = AudioAdapter(this)
        binding.musicList.layoutManager = LinearLayoutManager(this)
        binding.musicList.adapter = adapter
    }

    private fun initData() {
        adapter.setData(filesDir.scanAudioFile("aac","pcm"))
        adapter.setOnItemClickListener {

        }
    }


}