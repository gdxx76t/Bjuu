package com.sd.pianopro.midi

import android.media.midi.MidiDeviceService
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiReceiver
import android.util.Log

/**
 * سرویس MIDI برای دریافت پیام‌ها از دستگاه‌های خارجی
 * ایده توسط S.D داده شده
 */
class MidiService : MidiDeviceService() {
    
    companion object {
        private const val TAG = "MidiService"
    }
    
    private val midiReceiver = object : MidiReceiver() {
        override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
            // پردازش پیام‌های MIDI دریافتی
            processMidiMessage(msg, offset, count)
        }
    }
    
    override fun onGetInputPortReceivers(): Array<MidiReceiver> {
        return arrayOf(midiReceiver)
    }
    
    override fun onDeviceStatusChanged(status: MidiDeviceStatus) {
        super.onDeviceStatusChanged(status)
        Log.d(TAG, "MIDI device status changed: $status")
    }
    
    /**
     * پردازش پیام‌های MIDI
     */
    private fun processMidiMessage(msg: ByteArray, offset: Int, count: Int) {
        if (count < 3) return
        
        val status = msg[offset].toInt() and 0xFF
        val data1 = msg[offset + 1].toInt() and 0xFF
        val data2 = msg[offset + 2].toInt() and 0xFF
        
        Log.d(TAG, "MIDI message received: status=$status, data1=$data1, data2=$data2")
        
        // ارسال پیام به MainActivity یا AudioEngine
        // می‌توان از Broadcast یا EventBus استفاده کرد
    }
}

