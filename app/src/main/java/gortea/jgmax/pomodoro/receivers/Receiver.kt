package gortea.jgmax.pomodoro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class Receiver(private val onReceiveResult: (Context?, Intent?) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        onReceiveResult(context, intent)
    }
}