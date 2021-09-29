package com.ngsaihor.medialearning

import com.ngsaihor.medialearning.mdeia.audio.AudioFileModel
import java.io.File

val Int.formatTime: String
    get() {
        val second = this % 60
        val minuteTemp = this / 60
        return if (minuteTemp > 0) {
            val minute = minuteTemp % 60
            val hour = minuteTemp / 60
            if (hour > 0) {
                ((if (hour > 10) hour.toString() + "" else "0$hour") + ":" + (if (minute > 10) minute.toString() + "" else "0$minute")
                        + ":" + if (second > 10) second.toString() + "" else "0$second")
            } else {
                ("00:" + (if (minute > 10) minute.toString() + "" else "0$minute") + ":"
                        + if (second > 10) second.toString() + "" else "0$second")
            }
        } else {
            "00:00:" + if (second > 10) second.toString() + "" else "0$second"
        }
    }

fun File.scanAudioFile(vararg strArray : String): List<AudioFileModel> {
    return this.listFiles()?.filter {
        strArray.contains(it.extension)
    }?.map {
        AudioFileModel(it.name, it.path, it.length())
    } ?: mutableListOf()
}
