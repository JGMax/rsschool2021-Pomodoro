package gortea.jgmax.pomodoro.constants

import android.media.RingtoneManager

const val INVALID = "INVALID"
const val COMMAND_START = "START"
const val COMMAND_STOP = "STOP"
const val COMMAND_ID = "COMMAND_ID"

const val CHANNEL_ID = "Pomodoro notification"
const val NOTIFICATION_ID = 777

val NOTIFICATION_SOUND_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

const val DEFAULT_HOUR = 0
const val DEFAULT_MINUTE = 1
