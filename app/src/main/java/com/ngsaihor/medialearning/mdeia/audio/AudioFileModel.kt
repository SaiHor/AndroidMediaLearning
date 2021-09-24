package com.ngsaihor.medialearning.mdeia.audio

import java.text.DecimalFormat

data class AudioFileModel(val fileName: String = "", val filePath: String = "", val fileSize: Long = 0L) {
    fun getShowFileSize(): String {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        var wrongSize = "0B"
        if (fileSize < 1024) {
            fileSizeString = df.format(fileSize) + "B"
        } else if (fileSize < 1048576) {
            fileSizeString = df.format(fileSize / 1024) + "KB"
        } else if (fileSize < 1073741824) {
            fileSizeString = df.format(fileSize / 1048576) + "MB"
        } else {
            fileSizeString = df.format(fileSize / 1073741824) + "GB"
        }
        return fileSizeString
    }
}