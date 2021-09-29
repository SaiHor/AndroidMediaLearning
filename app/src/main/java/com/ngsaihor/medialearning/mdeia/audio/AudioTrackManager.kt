package com.ngsaihor.medialearning.mdeia.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.lang.ref.WeakReference

typealias RecordPlayStateListener = (Int) -> Unit
typealias RecordPlayTimerListener = (Int) -> Unit

object AudioTrackManager {

    const val STATE_PLAYING = 1
    const val STATE_PAUSE = 2
    const val STATE_STOP = 3

    private lateinit var track: AudioTrack
    private var bufferSize = 0
    private var stateListener: RecordPlayStateListener? = null
    private var timerListener: RecordPlayTimerListener? = null
    private var currentFilePath: String? = null

    fun setStateListener(stateListener: AudioRecordStateListener) {
        this@AudioTrackManager.stateListener = stateListener
    }

    fun setTimerListener(timerListener: AudioRecordTimerListener) {
        this@AudioTrackManager.timerListener = timerListener
    }

    private val handler: WeakHandler = WeakHandler(this)

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        val audioFormat: AudioFormat = AudioFormat.Builder()
            .setSampleRate(AudioConfig.SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()
        bufferSize =
            AudioTrack.getMinBufferSize(
                AudioConfig.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO,
                AudioConfig.AUDIO_FORMAT
            )
        track = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }

    var currentTime = 0
    suspend fun playPcmByFileName(filePath: String) {
        if (track.state != AudioTrack.STATE_INITIALIZED) {
            return
        }
        currentFilePath = filePath
        withContext(Dispatchers.Main) {
            stateListener?.invoke(STATE_PLAYING)
            handler.sendEmptyMessageDelayed(TIME_FLAG, 1000)
        }
        track.play()
        play()
    }

    private suspend fun play() {
        withContext(Dispatchers.IO) {
            try {
                val audioData = ByteArray(bufferSize)
                val fileInputStream = FileInputStream(currentFilePath)
                while (fileInputStream.available() > 0) {
                    val readCount: Int = fileInputStream.read(audioData)
                    if (readCount == -1) {
                        Log.e("TAG", "没有更多数据可以读取了")
                        break
                    }
                    val result = track.write(audioData, 0, readCount)
                    if (result < 0) {
                        continue
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    stateListener?.invoke(STATE_STOP)
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }
    }

    fun pauseMusic() {
        stateListener?.invoke(STATE_PAUSE)
        handler.removeCallbacksAndMessages(null)
        track.pause()
    }

    suspend fun resumeMusic() {
        withContext(Dispatchers.Main){
            stateListener?.invoke(STATE_PLAYING)
            handler.sendEmptyMessageDelayed(TIME_FLAG, 1000)
        }
        if (track.playState == AudioTrack.PLAYSTATE_PAUSED && currentFilePath?.isNotBlank() == true) {
            track.flush()
            track.play()
            play()
        }
    }

    fun stopMusic() {
        stateListener?.invoke(STATE_STOP)
        handler.removeCallbacksAndMessages(null)
        currentTime = 0
        track.stop()
    }

    private const val TIME_FLAG = 101

    private class WeakHandler(panelView: AudioTrackManager) : Handler() {

        private val weakReference: WeakReference<AudioTrackManager> =
            WeakReference<AudioTrackManager>(panelView)

        override fun handleMessage(msg: Message) {
            val manager: AudioTrackManager? = weakReference.get()
            if (manager == null) {
                super.handleMessage(msg)
                return
            }
            when (msg.what) {
                TIME_FLAG -> {
                    manager.timerListener?.invoke(++manager.currentTime)
                    manager.handler.sendEmptyMessageDelayed(TIME_FLAG, 1000)
                }
            }
            super.handleMessage(msg)
        }

    }

}