package net.coding.program.common.event

import org.greenrobot.eventbus.EventBus

class EventLoginSuccess {
    companion object {
        fun sendMessage() {
            EventBus.getDefault().post(EventLoginSuccess())
        }
    }
}
