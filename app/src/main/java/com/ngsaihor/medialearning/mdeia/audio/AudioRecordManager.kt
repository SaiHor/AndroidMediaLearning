package com.ngsaihor.medialearning.mdeia.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.LinkedBlockingDeque

object AudioRecordManager {

    private lateinit var audioRecord: AudioRecord
    private var bufferSize = 0
    private lateinit var bufferData: ByteArray

    private var isRecording = false
    private var audioQueue: LinkedBlockingDeque<ByteArray> = LinkedBlockingDeque()

    private var fileName = ""
    private var filePath = ""
    private var outputFileName = ""


    fun setFilePathAndName(path:String,outputName:String){
        this.filePath = path
        this.outputFileName = outputName
    }

    private fun initAudioRecord() {
        bufferSize =
            AudioRecord.getMinBufferSize(AudioConfig.SAMPLE_RATE, AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            AudioConfig.SAMPLE_RATE,AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        bufferData = ByteArray(bufferSize)
    }

    suspend fun startRecord(isResume: Boolean = false) {
        initAudioRecord()
        isRecording = true
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
                    val audioData = ByteArray(bufferSize)
                    val read = audioRecord.read(audioData, 0, bufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            audioQueue.add(audioData)
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
        isRecording = false
        audioRecord.stop()
    }

    suspend fun resume() {
        if (fileName.isBlank()) {
            return
        }
        startRecord(true)
    }

    fun stop() {
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
        AudioEncodeManager.encodeAction(audioQueue,"$filePath/$outputFileName")
        fileName = ""
        audioQueue.clear()
    }


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
                writeWaveFileHeader(outStream, fileLength, wavFileLength, 44100L, channels, byteRate)
                while (inStream.read(data) != -1) {
                    outStream.write(data)
                }
                inStream.close()
                outStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val file = File(outFileName)
                Log.d("AudioRecordManager", "outFile path:${file.absolutePath} size:${file.length()}")
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