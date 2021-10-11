package com.ngsaihor.medialearning.mdeia.video

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.concurrent.LinkedBlockingQueue

object VideoEncoderManager {

    private lateinit var mediaCodec: MediaCodec
    private lateinit var encodeFormat: MediaFormat
    private lateinit var outputStream: BufferedOutputStream
    private var outPutFilePath = ""
    private var outputFileName = ""
    private var videoBufferQueue: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue()
    var isRunning = false


    fun initEncoder(outputFilePath: String, width: Int, height: Int) {
        this.outPutFilePath = outputFilePath
        outputFileName = "${System.currentTimeMillis()}.mp4"
        initEncoder(width, height)
        Log.d("xxx","fileName:$outPutFilePath/$outputFileName")
        outputStream = BufferedOutputStream(FileOutputStream("$outPutFilePath/$outputFileName"))
    }

    private fun initEncodeFormat(width: Int, height: Int) {
        encodeFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        encodeFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        )
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5)
        encodeFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        encodeFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    private fun initEncoder(width: Int, height: Int) {
        initEncodeFormat(width, height)
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec.configure(
            encodeFormat,
            null,
            null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )
    }

    fun putData(yuv: ByteArray) {
        videoBufferQueue.put(yuv)
    }

    suspend fun startEncode() {
        isRunning = true
        mediaCodec.start()
        val bufferInfo = MediaCodec.BufferInfo()
        var configByte:ByteArray?=null
        withContext(Dispatchers.IO) {
            while (isRunning) {
                while (videoBufferQueue.isNotEmpty()) {
                    mediaCodec.dequeueInputBuffer(0).takeIf { it >= 0 }?.let { index ->
                        val inputBuffer = mediaCodec.getInputBuffer(index)
                        val size = videoBufferQueue.peek().size
                        inputBuffer?.clear()
                        inputBuffer?.put(videoBufferQueue.poll())
                        mediaCodec.queueInputBuffer(index, 0, size, System.currentTimeMillis(), 0)
                    }

                    val outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                    if (outputIndex > 0){
                        val outBuffer = mediaCodec.getOutputBuffer(outputIndex)
                        outBuffer?.position(bufferInfo.offset)
                        outBuffer?.limit(bufferInfo.size + bufferInfo.offset)
                        val outputByteBuffer = ByteArray(bufferInfo.size)
                        outBuffer?.get(outputByteBuffer)
                        if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                            configByte = ByteArray(bufferInfo.size)
                            configByte = outputByteBuffer
                        } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME && configByte!= null) {
                            val configByteSize = configByte?.size?:0
                            val keyframe = ByteArray(bufferInfo.size + configByteSize)
                            System.arraycopy(configByte, 0, keyframe, 0, configByteSize)
                            System.arraycopy(
                                configByte,
                                0,
                                keyframe,
                                configByteSize,
                                outputByteBuffer.size
                            )
                            outputStream.write(keyframe, 0, keyframe.size)
                        } else {
                            outputStream.write(outputByteBuffer, 0, outputByteBuffer.size)
                        }

                        try {
                            outputStream.write(outputByteBuffer, 0, outputByteBuffer.size)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        mediaCodec.releaseOutputBuffer(outputIndex, false);
                    }
                }
            }
            mediaCodec.stop()
            mediaCodec.release()
            outputStream.close()
        }

    }



}