package com.ngsaihor.medialearning.mdeia.video

import android.media.*
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer


object MediaHandleManager {
    private var videoExtractor: MediaExtractor = MediaExtractor()
    private var audioExtractor: MediaExtractor = MediaExtractor()
    private lateinit var mediaMediaMuxer: MediaMuxer
    private var videoSourceFile: File? = null
    private var audioSourceFile: File? = null
    private var outputFile: File? = null
    private var videoFormat: MediaFormat? = null
    private var audioFormat: MediaFormat? = null
    private var videoFormatRate = 0
    private var audioFormatRate = 0
    private var videoSelectTrackIndex = -1
    private var audioSelectTrackIndex = -1

    fun setInputAndOutputFilePath(videoSourceFilePath: String,audioSourceFilePath:String,outputFilePath: String): MediaHandleManager {
        videoSourceFile = File(videoSourceFilePath)
        audioSourceFile = File(audioSourceFilePath)
        outputFile = File(outputFilePath)
        videoExtractor.setDataSource(videoSourceFile?.absolutePath ?: "")
        audioExtractor.setDataSource(audioSourceFile?.absolutePath ?: "")
        return this
    }

    private suspend fun getTrackFormatList(extractor: MediaExtractor): List<MediaFormat> {
        return withContext(Dispatchers.IO) {
            val trackCount = extractor.trackCount
            val list: MutableList<MediaFormat> = mutableListOf()
            for (index in 0 until trackCount) {
                Log.d(
                    "MediaHandleManager",
                    "KEY_MIME:${videoExtractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)}"
                )
                list.add(extractor.getTrackFormat(index))
            }
            list.toList()
        }
    }

    suspend fun selectVideoTrack(): MediaHandleManager {
        getTrackFormatList(videoExtractor).forEachIndexed { index, mediaFormat ->
            if ((mediaFormat.getString(MediaFormat.KEY_MIME) ?: "").startsWith("video/")) {
                videoExtractor.selectTrack(index)
                videoFormat = mediaFormat
                videoFormatRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
                videoSelectTrackIndex = index
                return@forEachIndexed
            }
        }
        return this
    }

    suspend fun selectAudioTrack(): MediaHandleManager {
        getTrackFormatList(audioExtractor).forEachIndexed { index, mediaFormat ->
            if ((mediaFormat.getString(MediaFormat.KEY_MIME) ?: "").startsWith("audio/")) {
                videoExtractor.selectTrack(index)
                audioFormat = mediaFormat
                audioFormatRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
                audioSelectTrackIndex = index
                return@forEachIndexed
            }
        }
        return this
    }

    fun initMediaMediaMuxer(): MediaHandleManager {
        mediaMediaMuxer = MediaMuxer(outputFile?.absolutePath ?: "", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        videoFormat?.let { mediaMediaMuxer.addTrack(it) }
        audioFormat?.let { mediaMediaMuxer.addTrack(it) }
        mediaMediaMuxer.setOrientationHint(getVideoOrientation())
        return this
    }

    private fun getVideoOrientation(): Int {
        val retrieverSrc = MediaMetadataRetriever()
        retrieverSrc.setDataSource(videoSourceFile?.absolutePath ?: "")
        val degreesString = retrieverSrc.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
        )
        if (degreesString != null) {
            val degrees = degreesString.toInt()
            if (degrees == 0 || degrees == 90 || degrees == 270 || degrees == 180) {
                return degrees
            }
        }
        return 0
    }

    suspend fun startMuxer(progress: (info: MediaCodec.BufferInfo) -> Unit) {
        mediaMediaMuxer.start()
        val videoBufferInfo = MediaCodec.BufferInfo()
        videoBufferInfo.presentationTimeUs = 0
        val audioBufferInfo = MediaCodec.BufferInfo()
        audioBufferInfo.presentationTimeUs = 0
        val videoBuffer = ByteBuffer.allocate(500 * 1024)
        val audioBuffer = ByteBuffer.allocate(500 * 1024)
        withContext(Dispatchers.IO) {
            var videoSampleSize = videoExtractor.readSampleData(videoBuffer, 0)
            while (videoSampleSize > 0 && videoSelectTrackIndex != -1) {
                videoBufferInfo.offset = 0
                videoBufferInfo.size = videoSampleSize
                videoBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
                videoBufferInfo.presentationTimeUs = videoExtractor.sampleTime
                Log.d("MediaHandleManager","${videoExtractor.sampleTime}")
                withContext(Dispatchers.Main) {
                    progress.invoke(videoBufferInfo)
                }
                mediaMediaMuxer.writeSampleData(videoSelectTrackIndex, videoBuffer, videoBufferInfo)
                videoExtractor.advance()
                videoSampleSize = videoExtractor.readSampleData(videoBuffer, 0)
            }
            var audioSampleSize = audioExtractor.readSampleData(audioBuffer, 0)
            while (audioSampleSize > 0 && audioSelectTrackIndex != -1) {
                audioBufferInfo.offset = 0
                audioBufferInfo.size = audioSampleSize
                audioBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
                audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
                mediaMediaMuxer.writeSampleData(audioSelectTrackIndex, audioBuffer, audioBufferInfo)
                audioExtractor.advance()
                audioSampleSize = videoExtractor.readSampleData(audioBuffer, 0)
            }
            release()
        }
    }

    private fun release() {
        videoExtractor.release()
        mediaMediaMuxer.stop()
        mediaMediaMuxer.release()
    }
}