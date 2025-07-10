package com.sd.pianopro.midi

import android.content.Context
import android.media.midi.*
import android.util.Log
import com.sd.pianopro.audio.AudioEngine

/**
 * مدیریت اتصال و ارتباط با دستگاه‌های MIDI
 * ایده توسط S.D داده شده
 */
class MidiManager(private val context: Context, private val audioEngine: AudioEngine) {
    
    companion object {
        private const val TAG = "MidiManager"
    }
    
    private var midiManager: MidiManager? = null
    private var inputPort: MidiInputPort? = null
    private var outputPort: MidiOutputPort? = null
    private var midiDevice: MidiDevice? = null
    private val connectedDevices = mutableListOf<MidiDeviceInfo>()
    
    // Listener برای وضعیت اتصال MIDI
    private var onMidiConnectionListener: ((Boolean, String?) -> Unit)? = null
    
    // Receiver برای دریافت پیام‌های MIDI
    private val midiReceiver = object : MidiReceiver() {
        override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
            processMidiMessage(msg, offset, count)
        }
    }
    
    init {
        initializeMidiManager()
    }
    
    /**
     * راه‌اندازی MIDI Manager
     */
    private fun initializeMidiManager() {
        try {
            midiManager = context.getSystemService(Context.MIDI_SERVICE) as? MidiManager
            scanForMidiDevices()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MIDI manager", e)
        }
    }
    
    /**
     * جستجو برای دستگاه‌های MIDI
     */
    fun scanForMidiDevices() {
        midiManager?.let { manager ->
            val devices = manager.devices
            connectedDevices.clear()
            connectedDevices.addAll(devices)
            
            Log.d(TAG, "Found ${devices.size} MIDI devices")
            devices.forEach { device ->
                Log.d(TAG, "MIDI Device: ${device.properties.getString(MidiDeviceInfo.PROPERTY_NAME)}")
            }
        }
    }
    
    /**
     * اتصال به دستگاه MIDI
     */
    fun connectToDevice(deviceInfo: MidiDeviceInfo) {
        try {
            midiManager?.openDevice(deviceInfo, { device ->
                midiDevice = device
                setupMidiPorts(device)
                onMidiConnectionListener?.invoke(true, deviceInfo.properties.getString(MidiDeviceInfo.PROPERTY_NAME))
                Log.d(TAG, "Connected to MIDI device: ${deviceInfo.properties.getString(MidiDeviceInfo.PROPERTY_NAME)}")
            }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to MIDI device", e)
            onMidiConnectionListener?.invoke(false, null)
        }
    }
    
    /**
     * تنظیم پورت‌های MIDI
     */
    private fun setupMidiPorts(device: MidiDevice) {
        try {
            // تنظیم input port برای دریافت پیام‌ها از دستگاه خارجی
            if (device.info.inputPortCount > 0) {
                inputPort = device.openInputPort(0)
                inputPort?.connect(midiReceiver)
                Log.d(TAG, "Input port connected")
            }
            
            // تنظیم output port برای ارسال پیام‌ها به دستگاه خارجی
            if (device.info.outputPortCount > 0) {
                outputPort = device.openOutputPort(0)
                Log.d(TAG, "Output port connected")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup MIDI ports", e)
        }
    }
    
    /**
     * پردازش پیام‌های MIDI دریافتی
     */
    private fun processMidiMessage(msg: ByteArray, offset: Int, count: Int) {
        if (count < 3) return
        
        val status = msg[offset].toInt() and 0xFF
        val data1 = msg[offset + 1].toInt() and 0xFF
        val data2 = msg[offset + 2].toInt() and 0xFF
        
        when (status and 0xF0) {
            0x90 -> { // Note On
                if (data2 > 0) { // Velocity > 0 means note on
                    val velocity = data2 / 127.0f
                    audioEngine.playNote(data1, velocity)
                    Log.d(TAG, "MIDI Note On: $data1, velocity: $velocity")
                } else { // Velocity = 0 means note off
                    audioEngine.stopNote(data1)
                    Log.d(TAG, "MIDI Note Off: $data1")
                }
            }
            0x80 -> { // Note Off
                audioEngine.stopNote(data1)
                Log.d(TAG, "MIDI Note Off: $data1")
            }
            0xB0 -> { // Control Change
                handleControlChange(data1, data2)
            }
        }
    }
    
    /**
     * مدیریت پیام‌های Control Change
     */
    private fun handleControlChange(controller: Int, value: Int) {
        when (controller) {
            7 -> { // Volume
                val volume = value / 127.0f
                audioEngine.setMasterVolume(volume)
                Log.d(TAG, "MIDI Volume change: $volume")
            }
            // می‌توان کنترل‌های بیشتری اضافه کرد
        }
    }
    
    /**
     * ارسال پیام MIDI
     */
    fun sendMidiMessage(status: Int, data1: Int, data2: Int) {
        try {
            val msg = byteArrayOf(status.toByte(), data1.toByte(), data2.toByte())
            outputPort?.send(msg, 0, 3)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send MIDI message", e)
        }
    }
    
    /**
     * ارسال Note On
     */
    fun sendNoteOn(note: Int, velocity: Int, channel: Int = 0) {
        sendMidiMessage(0x90 or channel, note, velocity)
    }
    
    /**
     * ارسال Note Off
     */
    fun sendNoteOff(note: Int, channel: Int = 0) {
        sendMidiMessage(0x80 or channel, note, 0)
    }
    
    /**
     * قطع اتصال از دستگاه MIDI
     */
    fun disconnect() {
        try {
            inputPort?.close()
            outputPort?.close()
            midiDevice?.close()
            
            inputPort = null
            outputPort = null
            midiDevice = null
            
            onMidiConnectionListener?.invoke(false, null)
            Log.d(TAG, "Disconnected from MIDI device")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting MIDI device", e)
        }
    }
    
    /**
     * بررسی وضعیت اتصال
     */
    fun isConnected(): Boolean = midiDevice != null
    
    /**
     * دریافت لیست دستگاه‌های موجود
     */
    fun getAvailableDevices(): List<MidiDeviceInfo> = connectedDevices.toList()
    
    /**
     * تنظیم listener برای وضعیت اتصال
     */
    fun setOnMidiConnectionListener(listener: (Boolean, String?) -> Unit) {
        onMidiConnectionListener = listener
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        disconnect()
    }
}

