package com.ngsaihor.medialearning.mdeia.audio

import android.media.AudioFormat

class AudioConfig {
    companion object{
        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_DEFAULT
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    }
}