package com.ngsaihor.medialearning.mdeia.audio

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream


object AudioEncodeManager {
    private lateinit var mediaCodec: MediaCodec
    private lateinit var encodeFormat: MediaFormat
    private var inputFilePath: String? = ""
    private var outputFilePath: String? = ""
    private var isStop = true

    suspend fun encodeAction(inputFilePath:String,outputFilePath:String){
        this.inputFilePath = inputFilePath
        this.outputFilePath = outputFilePath
        initEncodeFormat()
        initEncoder()
        encodeByFile()
    }

    private fun initEncodeFormat() {
        encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2)
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000)
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096)
    }

    private fun initEncoder() {
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        mediaCodec.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }


    private suspend fun encodeByFile() {
        val fis = FileInputStream(inputFilePath)
        isStop = false
        val out = BufferedOutputStream(FileOutputStream(outputFilePath, false))
        withContext(Dispatchers.IO) {
            val bufferByteArray = ByteArray(1024)
            val bufferInfo = MediaCodec.BufferInfo()
            mediaCodec.start()
            while (fis.read(bufferByteArray) != -1) {
                mediaCodec.dequeueInputBuffer(0).takeIf { it >= 0 }?.let { index ->
                    val inputBuffer = mediaCodec.getInputBuffer(index)
                    inputBuffer?.put(bufferByteArray)
                    mediaCodec.queueInputBuffer(index, 0, bufferByteArray.size, 0, 0)
                }
                val outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                if (outputIndex > 0) {
                    val outBuffer = mediaCodec.getOutputBuffer(outputIndex)
                    outBuffer?.position(bufferInfo.offset)
                    outBuffer?.limit(bufferInfo.offset + bufferInfo.size)

                    val outputByteBuffer = ByteArray(bufferInfo.size + 7)
                    addADTStoPacket(outputByteBuffer, bufferInfo.size + 7)
                    outBuffer?.get(outputByteBuffer,7,bufferInfo.size)
                    outBuffer?.position(bufferInfo.offset)

                    try {
                        out.write(outputByteBuffer,0,outputByteBuffer.size)
                        out.flush()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    outBuffer?.position(bufferInfo.offset)
                    mediaCodec.releaseOutputBuffer(outputIndex, false);
                }

            }
        }
    }

    private suspend fun encodeByBuffer() {

    }


    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        val profile = 2 // AAC LC
        val freqIdx = 4 // 44.1KHz
        val chanCfg = 2 // CPE

        // fill in ADTS data
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

    private fun getADTSampleRate(sampleRate: Int): Int {
        var rate = 4
        when (sampleRate) {
            96000 -> rate = 0
            88200 -> rate = 1
            64000 -> rate = 2
            48000 -> rate = 3
            44100 -> rate = 4
            32000 -> rate = 5
            24000 -> rate = 6
            22050 -> rate = 7
            16000 -> rate = 8
            12000 -> rate = 9
            11025 -> rate = 10
            8000 -> rate = 11
            7350 -> rate = 12
        }
        return rate
    }
}