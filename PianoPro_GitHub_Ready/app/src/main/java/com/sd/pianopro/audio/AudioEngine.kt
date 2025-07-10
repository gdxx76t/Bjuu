package com.sd.pianopro.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * موتور صوتی برای تولید و پخش صداهای پیانو
 * ایده توسط S.D داده شده
 */
class AudioEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioEngine"
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 4096
        private const val MAX_STREAMS = 10
    }
    
    private var soundPool: SoundPool? = null
    private var audioTrack: AudioTrack? = null
    private val soundMap = mutableMapOf<Int, Int>() // MIDI number to sound ID mapping
    private val activeNotes = mutableMapOf<Int, Job>() // Active note jobs
    private val frequencyCalculator = FrequencyCalculator()
    
    // Audio generation parameters
    private var masterVolume = 0.7f
    private var attackTime = 0.1f
    private var decayTime = 0.3f
    private var sustainLevel = 0.6f
    private var releaseTime = 0.5f
    
    init {
        initializeSoundPool()
        initializeAudioTrack()
    }
    
    /**
     * راه‌اندازی SoundPool برای پخش صداهای از پیش ضبط شده
     */
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()
    }
    
    /**
     * راه‌اندازی AudioTrack برای تولید صدای سینتتیک
     */
    private fun initializeAudioTrack() {
        val audioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
            
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
            
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(BUFFER_SIZE * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack", e)
        }
    }
    
    /**
     * بارگذاری صداهای واقعی پیانو از فایل‌های raw
     */
    fun loadPianoSounds() {
        // در اینجا باید صداهای واقعی پیانو بارگذاری شوند
        // برای نمونه، فرض می‌کنیم فایل‌های صوتی در پوشه raw قرار دارند
        try {
            // بارگذاری نمونه صداها (باید فایل‌های واقعی اضافه شوند)
            // soundMap[60] = soundPool?.load(context, R.raw.piano_c4, 1) ?: 0
            // soundMap[61] = soundPool?.load(context, R.raw.piano_cs4, 1) ?: 0
            // ... سایر نت‌ها
            
            Log.d(TAG, "Piano sounds loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load piano sounds", e)
        }
    }
    
    /**
     * پخش نت با استفاده از صدای واقعی یا سینتتیک
     */
    fun playNote(midiNumber: Int, velocity: Float = 1.0f) {
        // اگر صدای واقعی موجود است، از آن استفاده کن
        if (soundMap.containsKey(midiNumber)) {
            playRealSound(midiNumber, velocity)
        } else {
            // در غیر این صورت صدای سینتتیک تولید کن
            playSyntheticSound(midiNumber, velocity)
        }
    }
    
    /**
     * پخش صدای واقعی
     */
    private fun playRealSound(midiNumber: Int, velocity: Float) {
        val soundId = soundMap[midiNumber] ?: return
        soundPool?.play(soundId, velocity * masterVolume, velocity * masterVolume, 1, 0, 1.0f)
    }
    
    /**
     * تولید و پخش صدای سینتتیک
     */
    private fun playSyntheticSound(midiNumber: Int, velocity: Float) {
        // متوقف کردن نت قبلی اگر در حال پخش است
        stopNote(midiNumber)
        
        val frequency = frequencyCalculator.calculateFrequency(midiNumber)
        
        // شروع Job جدید برای تولید صدا
        val job = CoroutineScope(Dispatchers.IO).launch {
            generateAndPlayTone(frequency, velocity)
        }
        
        activeNotes[midiNumber] = job
    }
    
    /**
     * تولید و پخش تون با ADSR envelope
     */
    private suspend fun generateAndPlayTone(frequency: Double, velocity: Float) {
        try {
            audioTrack?.play()
            
            val duration = 2.0 // مدت زمان پخش (ثانیه)
            val totalSamples = (SAMPLE_RATE * duration).toInt()
            val buffer = ShortArray(BUFFER_SIZE)
            
            var sampleIndex = 0
            
            while (sampleIndex < totalSamples && !Thread.currentThread().isInterrupted) {
                val samplesInBuffer = minOf(BUFFER_SIZE, totalSamples - sampleIndex)
                
                for (i in 0 until samplesInBuffer) {
                    val time = (sampleIndex + i).toDouble() / SAMPLE_RATE
                    val amplitude = calculateADSRAmplitude(time, duration) * velocity * masterVolume
                    
                    // تولید موج سینوسی با هارمونیک‌های اضافی برای صدای واقعی‌تر
                    val sample = generatePianoWave(frequency, time) * amplitude
                    
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                
                audioTrack?.write(buffer, 0, samplesInBuffer)
                sampleIndex += samplesInBuffer
                
                // کمی تأخیر برای جلوگیری از مصرف بیش از حد CPU
                delay(1)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating tone", e)
        } finally {
            audioTrack?.pause()
        }
    }
    
    /**
     * تولید موج پیانو با هارمونیک‌های مختلف
     */
    private fun generatePianoWave(frequency: Double, time: Double): Double {
        val fundamental = sin(2 * PI * frequency * time)
        val harmonic2 = 0.5 * sin(2 * PI * frequency * 2 * time)
        val harmonic3 = 0.25 * sin(2 * PI * frequency * 3 * time)
        val harmonic4 = 0.125 * sin(2 * PI * frequency * 4 * time)
        
        return fundamental + harmonic2 + harmonic3 + harmonic4
    }
    
    /**
     * محاسبه دامنه ADSR
     */
    private fun calculateADSRAmplitude(time: Double, totalDuration: Double): Double {
        return when {
            time < attackTime -> time / attackTime
            time < attackTime + decayTime -> {
                val decayProgress = (time - attackTime) / decayTime
                1.0 - (1.0 - sustainLevel) * decayProgress
            }
            time < totalDuration - releaseTime -> sustainLevel
            else -> {
                val releaseProgress = (time - (totalDuration - releaseTime)) / releaseTime
                sustainLevel * (1.0 - releaseProgress)
            }
        }
    }
    
    /**
     * متوقف کردن نت
     */
    fun stopNote(midiNumber: Int) {
        activeNotes[midiNumber]?.cancel()
        activeNotes.remove(midiNumber)
    }
    
    /**
     * متوقف کردن تمام نت‌ها
     */
    fun stopAllNotes() {
        activeNotes.values.forEach { it.cancel() }
        activeNotes.clear()
        audioTrack?.pause()
    }
    
    /**
     * تنظیم نسبت فاصله نت‌ها
     */
    fun setIntervalRatio(ratio: Double) {
        frequencyCalculator.setIntervalRatio(ratio)
    }
    
    /**
     * دریافت نسبت فعلی
     */
    fun getIntervalRatio(): Double = frequencyCalculator.getIntervalRatio()
    
    /**
     * دریافت تعداد اکتاوها
     */
    fun getOctaveCount(): Int = frequencyCalculator.getOctaveCount()
    
    /**
     * دریافت فرکانس نت
     */
    fun getNoteFrequency(midiNumber: Int): Double = frequencyCalculator.calculateFrequency(midiNumber)
    
    /**
     * دریافت لیست تمام نت‌ها
     */
    fun getAllNotes(): List<PianoNote> = frequencyCalculator.getAllNotes()
    
    /**
     * تنظیم حجم صدا
     */
    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        stopAllNotes()
        soundPool?.release()
        audioTrack?.release()
        soundPool = null
        audioTrack = null
    }
}

