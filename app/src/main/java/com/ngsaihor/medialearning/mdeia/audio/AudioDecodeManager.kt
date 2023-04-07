package com.ngsaihor.medialearning.mdeia.audio

import android.media.*
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object AudioDecodeManager {
    private lateinit var mediaCodec: MediaCodec
    private var audioExtractor: MediaExtractor = MediaExtractor()
    private var isStop = true
    private val rootSd = Environment.getExternalStorageDirectory()
    private val output = File(rootSd?.absolutePath.toString() + "/test10086/pcm_output.pcm")
    private var outputFilePath: String = output.absolutePath
    private lateinit var audioTrack: AudioTrack

    suspend fun transformAudioFileToPCM(filePath: String) {
        if (initAudioDecoderByFilePath(filePath)) {
            startDecodeAACToPCM()
        } else {
            Log.d("AudioCodecManager", "initFailed")
        }
    }

    suspend fun playAudio(filePath: String) {
        initAudioDecoderByFilePath(filePath)
        startPlayAudio()
    }

    private fun initAudioDecoderByFilePath(filePath: String): Boolean {
        audioExtractor.setDataSource(filePath)
        val trackCount = audioExtractor.trackCount
        for (index in 0 until trackCount) {
            val format = audioExtractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (!mime.isNullOrEmpty() && mime.startsWith("audio/")) {
                audioExtractor.selectTrack(index)
                val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
                mediaCodec = MediaCodec.createByCodecName(codecList.findDecoderForFormat(format))
                try {
                    mediaCodec.configure(format, null, null, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
                mediaCodec.start()
                return true
            }
        }
        return false
    }

    //转换文件
    private suspend fun startDecodeAACToPCM() {
        val outputBufferInfo = MediaCodec.BufferInfo()
        isStop = false
        withContext(Dispatchers.IO) {
            val fos = FileOutputStream(outputFilePath)
            while (!isStop) {
                mediaCodec.dequeueInputBuffer(1000).takeIf { it >= 0 }?.let { inputIndex->
                    val inputBuffer = mediaCodec.getInputBuffer(inputIndex)
                    inputBuffer?.let {
                        val sampleSize = audioExtractor.readSampleData(inputBuffer, 0)
                        if (sampleSize > 0) {
                            mediaCodec.queueInputBuffer(inputIndex, 0, sampleSize, 1000, 0)
                            audioExtractor.advance()
                        } else {
                            mediaCodec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        }
                    }
                }

                when (val outputIndex = mediaCodec.dequeueOutputBuffer(outputBufferInfo, 1000)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d(
                            "MediaCodecManager",
                            "INFO_OUTPUT_FORMAT_CHANGED format : " + mediaCodec.outputFormat
                        )
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        Log.d("MediaCodecManager", "INFO_TRY_AGAIN_LATER")
                    }
                    else -> {
                        val outPutBuffer = mediaCodec.getOutputBuffer(outputIndex)
                        val buffer = ByteArray(outputBufferInfo.size)
                        outPutBuffer?.get(buffer)
                        outPutBuffer?.clear()
                        try {
                            fos.write(buffer, outputBufferInfo.offset, outputBufferInfo.offset + outputBufferInfo.size)
                            fos.flush()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        mediaCodec.releaseOutputBuffer(outputIndex, false)
                    }
                }
                if (outputBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    isStop = true
                    break
                }

            }
            fos.close()
            mediaCodec.stop()
            mediaCodec.release()
            audioExtractor.release()
        }
    }

    private var bufferSize = 0
    private fun initAudioTrack() {
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        val audioFormat: AudioFormat = AudioFormat.Builder()
            .setSampleRate(44100)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()
        bufferSize =
            AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        audioTrack = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }

    //直接播放
    private suspend fun startPlayAudio() {
        initAudioTrack()
        val outputBufferInfo = MediaCodec.BufferInfo()
        isStop = false
        withContext(Dispatchers.IO) {
            audioTrack.play()
            while (!isStop) {
                mediaCodec.dequeueInputBuffer(1000).takeIf { it >= 0 }?.let { index ->
                    val inputBuffer = mediaCodec.getInputBuffer(index)
                    inputBuffer?.let {
                        val sampleSize = audioExtractor.readSampleData(inputBuffer, 0)
                        if (audioExtractor.advance() && sampleSize > 0) {
                            mediaCodec.queueInputBuffer(index, 0, sampleSize, 1000, 0)
                        } else {
                            mediaCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        }
                    }
                }

                when (val outputIndex = mediaCodec.dequeueOutputBuffer(outputBufferInfo, 1000)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d(
                            "MediaCodecManager",
                            "INFO_OUTPUT_FORMAT_CHANGED format : " + mediaCodec.outputFormat
                        )
                        val format: MediaFormat = mediaCodec.outputFormat
                        audioTrack.playbackRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        Log.d("MediaCodecManager", "INFO_TRY_AGAIN_LATER")
                    }
                    else -> {
                        val outPutBuffer = mediaCodec.getOutputBuffer(outputIndex)
                        val buffer = ByteArray(outputBufferInfo.size)
                        outPutBuffer?.get(buffer)
                        outPutBuffer?.clear()
                        audioTrack.write(
                            buffer,
                            outputBufferInfo.offset,
                            outputBufferInfo.offset + outputBufferInfo.size
                        )
                        mediaCodec.releaseOutputBuffer(outputIndex, false)
                    }
                }
                if (outputBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    isStop = true
                    break
                }

            }
            mediaCodec.stop()
            mediaCodec.release()
            audioExtractor.release()
        }
    }
}