package gortea.jgmax.pomodoro.constants

import android.media.RingtoneManager

const val INVALID = "INVALID"
const val COMMAND_START = "START"
const val COMMAND_STOP = "STOP"
const val COMMAND_ID = "COMMAND_ID"

const val CHANNEL_ID = "Pomodoro notification"
const val NOTIFICATION_ID = 777

const val CURRENT_TIME_KEY = "CURRENT_TIME"
const val CURRENT_ID_KEY = "CURRENT_ID"

const val RESULT_INTENT_FILTER = "SERVICE_RESULT_INTENT"

val NOTIFICATION_SOUND_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

const val DEFAULT_HOUR = 0
const val DEFAULT_MINUTE = 1
