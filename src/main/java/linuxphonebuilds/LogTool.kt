package linuxphonebuilds

import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class LogTool {
    private val logFile by lazy {
        val file = File("log.txt")
        if(!file.exists()) {
            file.createNewFile()
        }
        file
    }

    private val dateFormat by lazy {
        SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    }

    fun log(msg: String) {
        val date = Date()
        logFile.appendText(dateFormat.format(date) + ": " + msg + "\n")
    }
}