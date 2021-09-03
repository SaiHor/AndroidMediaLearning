package com.ngsaihor.medialearning.mdeia.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream


object AudioTrackManager {

    private lateinit var track: AudioTrack
    private var bufferSize = 0

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setHapticChannelsMuted(false)
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

    fun playPcmByFileName(filePath: String, context: AppCompatActivity) {
        if (track.state != AudioTrack.STATE_INITIALIZED) {
            return
        }
//        val filePath = context.cacheDir.absolutePath + "/" + fileName
        track.play()
        context.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val audioData = ByteArray(bufferSize)
                val fileInputStream = FileInputStream(filePath)
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
            }
        }
    }

    fun pauseMusic() {
        track.pause()
    }

    fun resumeMusic() {
        if (track.playState == AudioTrack.PLAYSTATE_PAUSED) {
            track.play()
        }
    }

    fun stopMusic() {
        track.stop()
    }

    suspend fun getPcmList(context: AppCompatActivity): List<PcmFileModel> {
        return withContext(Dispatchers.IO) {
            context.cacheDir.listFiles().filter {
                it != null && it.exists() && it.isFile && it.name.substring(it.name.lastIndexOf("."))
                    .lowercase() == ".pcm".lowercase()
            }.map {
                PcmFileModel(it.name, it.path, it.length())
            }
        }
    }

}