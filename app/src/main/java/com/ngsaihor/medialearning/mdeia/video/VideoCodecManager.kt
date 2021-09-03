package com.ngsaihor.medialearning.mdeia.video

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


object VideoCodecManager {

    private lateinit var mediaCodec: MediaCodec
    private var videoExtractor: MediaExtractor = MediaExtractor()
    private var videoIsStop = true

    suspend fun playVideoByFilePathWithSurface(filePath: String, surface: Surface) {
        if (initVideoDecoderByFilePath(filePath, surface)) {
            startPlayVideo()
        } else {
            Log.d("MediaCodecManager", "initFailed")
        }
    }

    private fun initVideoDecoderByFilePath(filePath: String, surface: Surface): Boolean {
        videoExtractor.setDataSource(filePath)
        val trackCount = videoExtractor.trackCount
        for (index in 0 until trackCount) {
            val format = videoExtractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("video/") == true) {
                videoExtractor.selectTrack(index)
                val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
                mediaCodec = MediaCodec.createByCodecName(codecList.findDecoderForFormat(format))
                try {
                    mediaCodec.configure(format, surface, null, 0)
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


    private suspend fun startPlayVideo() {

        var firstLoad = true
        var startTime = 0L
        videoIsStop = false
        val outputBufferInfo = MediaCodec.BufferInfo()
        withContext(Dispatchers.IO) {
            while (!videoIsStop) {
                mediaCodec.dequeueInputBuffer(1000).takeIf { it >= 0 }?.let { index ->
                    val inputBuffer = mediaCodec.getInputBuffer(index)
                    inputBuffer?.let {
                        val sampleSize = videoExtractor.readSampleData(inputBuffer, 0)
                        if (videoExtractor.advance() && sampleSize > 0) {
                            mediaCodec.queueInputBuffer(index, 0, sampleSize, videoExtractor.sampleTime, 0)
                        } else {
                            mediaCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        }
                    }
                }
                when (val outIndex = mediaCodec.dequeueOutputBuffer(outputBufferInfo, 1000)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d("MediaCodecManager", "INFO_OUTPUT_FORMAT_CHANGED format : " + mediaCodec.outputFormat)
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        Log.d("MediaCodecManager", "INFO_TRY_AGAIN_LATER")
                    }
                    else -> {
                        //为了防止频繁报INFO_TRY_AGAIN_LATER，需要delay
                        var delayTime = 0L
                        if (firstLoad) {
                            firstLoad = false
                            startTime = System.currentTimeMillis()
                        }
                        delayTime =
                            outputBufferInfo.presentationTimeUs / 1000 - (System.currentTimeMillis() - startTime)
                        delay(delayTime)
                        mediaCodec.releaseOutputBuffer(outIndex, true)
                    }
                }
                if (outputBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    videoIsStop = true
                    break
                }
            }
            mediaCodec.stop()
            mediaCodec.release()
            videoExtractor.release()
        }
    }

    fun stopPlayVideo() {
        videoIsStop = true
        mediaCodec.stop()
        mediaCodec.release()
        videoExtractor.release()
    }

}