package com.ngsaihor.medialearning.mdeia.audio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ngsaihor.medialearning.databinding.ActivityAudioRecordBinding
import com.ngsaihor.medialearning.mdeia.audio.list.AudioAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioRecordBinding
    private lateinit var adapter: AudioAdapter
    private var filePath: String? = null
    private var isPause = false
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AudioRecordManager.setFilePathAndName(filesDir.absolutePath)
        AudioRecordManager.setStateListener {
            when (it) {
                AudioRecordManager.STATE_PLAYING -> {
                    binding.start.isEnabled = false
                    binding.pause.isVisible = true
                    binding.stop.isVisible = true
                    binding.pause.text = "暂停录音"
                }
                AudioRecordManager.STATE_PAUSE -> {
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
            binding.progress.text = formatTime(it)
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
                isPause = false
            }
            pause.setOnClickListener {
                if (isPause) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        AudioRecordManager.startRecord(true)
                    }
                } else {
                    AudioRecordManager.pause()
                }
                isPause = !isPause
            }
            stop.setOnClickListener {
                AudioRecordManager.stop()
                binding.root.postDelayed({
                    adapter.setData(scanFile())
                }, 100)
            }
        }
        adapter = AudioAdapter(this)
        binding.musicList.layoutManager = LinearLayoutManager(this)
        binding.musicList.adapter = adapter
    }

    private fun initData() {
        filePath = filesDir.absolutePath
        adapter.setData(scanFile())
    }

    private fun scanFile() = filesDir.listFiles()?.filter {
        it.extension.lowercase() == "aac" || it.extension.lowercase() == "pcm"
    }?.map {
        AudioFileModel(it.name, it.path, it.length())
    } ?: mutableListOf()

    fun formatTime(timeTemp: Int): String {
        val second = timeTemp % 60
        val minuteTemp = timeTemp / 60
        return if (minuteTemp > 0) {
            val minute = minuteTemp % 60
            val hour = minuteTemp / 60
            if (hour > 0) {
                ((if (hour > 10) hour.toString() + "" else "0$hour") + ":" + (if (minute > 10) minute.toString() + "" else "0$minute")
                        + ":" + if (second > 10) second.toString() + "" else "0$second")
            } else {
                ("00:" + (if (minute > 10) minute.toString() + "" else "0$minute") + ":"
                        + if (second > 10) second.toString() + "" else "0$second")
            }
        } else {
            "00:00:" + if (second > 10) second.toString() + "" else "0$second"
        }
    }

}