package utils

import kotlin.js.Date

/**
 * Structure defines log levels
 */
enum class LogLevel(level:String) {
    DEBUG("DEBUG"),
    WARNING("WARNING"),
    ERROR("ERROR"),
    INFO("INFO"),
    DEBUG_REDUX("DEBUG_REDUX")
}


/**
 * Global object, used to write logs to different targets: console. file. temporary browser
 * storage
 */
object Logger {

    // Array of enabled log levels. Messages with log level out of this list will
    // be silently ignored
    var loggerLevels = arrayOf(LogLevel.ERROR, LogLevel.WARNING, LogLevel.INFO, LogLevel.DEBUG)

    /**
     * Function used to format date to string
     * @param date: Input date
     * @return String representation of date
     */
    fun formatDate(date:Date):String {
        var month = date.getMonth()+1
        var hour = date.getHours()
        var minute = date.getMinutes()
        var second = date.getSeconds()
        var day = date.getDate()

        var yearStr = date.getFullYear().toString()
        var monthStr = month.toString();if (month<10) { monthStr = "0"+monthStr };
        var dayStr = day.toString();if (month<10) { dayStr = "0"+dayStr };
        var hourStr = hour.toString();if (hour<10) { hourStr = "0"+hourStr };
        var minuteStr = minute.toString();if (minute<10) { minuteStr = "0"+minuteStr };
        var secondStr = second.toString();if (second<10) { secondStr = "0"+secondStr };

        return yearStr+"-"+monthStr+"-"+dayStr+" "+hourStr+":"+minuteStr+":"+secondStr
    }

    /**
     * Function used to log message
     *
     * @param logLevel: Log level
     * @param message: Log message
     * @param className: Name of class, which called this function to log message
     * @param methodName: Name of method of class, which called this function to log mesasge
     */
    fun log(logLevel:LogLevel = LogLevel.INFO, message:String, className:String="",methodName:String="") {
        if (this.loggerLevels.contains(logLevel)) {
            var date = Date()
            console.log(this.formatDate(date)+" - "+logLevel.name+": "+message+" ("+className+","+methodName+")")
        }
    }

}