package gortea.jgmax.pomodoro.presenters

import gortea.jgmax.pomodoro.receivers.Receiver

interface ReceiverRegister {
    fun registerLocalReceiver(receiver: Receiver)
    fun unregisterLocalReceiver(receiver: Receiver)
}