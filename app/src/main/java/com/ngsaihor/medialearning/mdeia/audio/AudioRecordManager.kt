package com.ngsaihor.medialearning.mdeia.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Message
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.LinkedBlockingDeque

typealias AudioRecordStateListener = (Int) -> Unit
typealias AudioRecordTimerListener = (Int) -> Unit

object AudioRecordManager {
    const val STATE_PLAYING = 1
    const val STATE_PAUSE = 2
    const val STATE_STOP = 3

    private lateinit var audioRecord: AudioRecord
    private var bufferSize = 0

    private var isRecording = false
    private var audioQueue: LinkedBlockingDeque<ByteArray> = LinkedBlockingDeque()

    private var fileName = ""
    private var filePath = ""
    private var outputFileName = ""

    private val handler: WeakHandler = WeakHandler(this)

    private var stateListener: AudioRecordStateListener? = null
    private var timerListener: AudioRecordTimerListener? = null

    fun setStateListener(stateListener: AudioRecordStateListener) {
        this@AudioRecordManager.stateListener = stateListener
    }

    fun setTimerListener(timerListener: AudioRecordTimerListener) {
        this@AudioRecordManager.timerListener = timerListener
    }

    fun setFilePathAndName(path: String) {
        this.filePath = path
    }

    private fun initAudioRecord() {
        // 根据采样率，通道，音频格式计算最小缓冲区大小
        // 音频写入文件之前的步骤：1、设置一个缓冲区去暂时存放音频 2、然后再把缓冲区里的数据写入文件
        // 因此缓冲区大小需要>=单位音频的大小，调用getMinBufferSize可以获取最小缓冲区大小的值
        bufferSize =
            AudioRecord.getMinBufferSize(
                AudioConfig.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT
            )
        // 配置录制音频的录制器
        // 注意这里的配置需要和getMinBufferSize的一致
        // 比如如果AudioRecord设置的采样率>getMinBufferSize设置的采样率
        // 那么会因为缓冲区大小不够而导致录制失败
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            AudioConfig.SAMPLE_RATE, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
    }

    var currentTime = 0
    suspend fun startRecord(isResume: Boolean = false) {
        initAudioRecord()
        isRecording = true
        withContext(Dispatchers.Main) {
            stateListener?.invoke(STATE_PLAYING)
            handler.sendEmptyMessageDelayed(TIME_FLAG, 1000)
        }
        // 开始录制
        audioRecord.startRecording()
        if (!isResume) {
            fileName = "${System.currentTimeMillis()}.pcm"
        }
        withContext(Dispatchers.IO) {
            val filePath = "$filePath/$fileName"
            var os: FileOutputStream? = null
            try {
                os = FileOutputStream(filePath, isResume)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (os != null) {
                while (isRecording) {
                    // 缓冲区
                    val audioData = ByteArray(bufferSize)

                    // 通过read的方法把单位音频写入缓冲区
                    val read = audioRecord.read(audioData, 0, bufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            audioQueue.add(audioData)

                            // 把缓冲区的数据写入文件
                            os.write(audioData)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                try {
                    os.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun pause() {
        handler.removeCallbacksAndMessages(null)
        stateListener?.invoke(STATE_PAUSE)
        isRecording = false
        audioRecord.stop()
    }

    suspend fun resume() {
        if (fileName.isBlank()) {
            return
        }
        stateListener?.invoke(STATE_PLAYING)
        startRecord(true)
    }

    fun stop() {
        currentTime = 0
        handler.removeCallbacksAndMessages(null)
        stateListener?.invoke(STATE_STOP)
        pause()
        fileName = ""
        audioQueue.clear()
    }

    suspend fun stopAndTransformToWav() {
        pause()
        pcmToWav()
        fileName = ""
        audioQueue.clear()
    }

    suspend fun stopAndTransformToAAC() {
        pause()
        outputFileName = "${System.currentTimeMillis()}.aac"
        AudioEncodeManager.encodeAction(audioQueue, "$filePath/$outputFileName")
        fileName = ""
        audioQueue.clear()
    }

    private const val TIME_FLAG = 101

    private class WeakHandler(panelView: AudioRecordManager) : Handler() {

        private val weakReference: WeakReference<AudioRecordManager> =
            WeakReference<AudioRecordManager>(panelView)

        override fun handleMessage(msg: Message) {
            val manager: AudioRecordManager? = weakReference.get()
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

    // 录制的音频格式是pcm
    // pcm转wav方法
    // wav是带头wav文件头的pcm容器
    // 所以pcm转wav的方法就是通过写入wav的文件头，再写入pcm数据
    private suspend fun pcmToWav() {
        val inFileName = filePath + fileName
        val outFileName = filePath + "${System.currentTimeMillis()}.wav"

        val file = File(inFileName)
        Log.d("AudioRecordManager", "inFile path:${file.absolutePath} size:${file.length()}")

        Log.d("AudioRecordManager", "inFile:${inFileName}")
        Log.d("AudioRecordManager", "outFile:${outFileName}")
        withContext(Dispatchers.IO) {
            try {
                val data = ByteArray(bufferSize)
                val inStream = FileInputStream(inFileName)
                val outStream = FileOutputStream(outFileName)
                val fileLength = inStream.channel.size()
                val wavFileHeaderSize = 36
                val wavFileLength = fileLength + wavFileHeaderSize
                val channels = 2
                val byteRate: Long = 16L * 44100L * channels / 8L
                writeWaveFileHeader(
                    outStream,
                    fileLength,
                    wavFileLength,
                    44100L,
                    channels,
                    byteRate
                )
                while (inStream.read(data) != -1) {
                    outStream.write(data)
                }
                inStream.close()
                outStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val file = File(outFileName)
                Log.d(
                    "AudioRecordManager",
                    "outFile path:${file.absolutePath} size:${file.length()}"
                )
            }
        }
    }

    private fun writeWaveFileHeader(
        out: FileOutputStream, totalAudioLen: Long,
        totalDataLen: Long, longSampleRate: Long,
        channels: Int, byteRate: Long
    ) {
        val header = ByteArray(44)
        // RIFF/WAVE header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        //WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // 'fmt ' chunk
        // 'fmt ' chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        // 4 bytes: size of 'fmt ' chunk
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // format = 1
        // format = 1
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        // block align
        header[32] = (2 * 16 / 8).toByte()
        header[33] = 0
        // bits per sample
        header[34] = 16
        header[35] = 0
        //data
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }


}