package com.sd.pianopro.audio

import kotlin.math.pow

/**
 * کلاس محاسبه فرکانس نت‌ها با قابلیت تنظیم نسبت فاصله
 * ایده توسط S.D داده شده
 */
class FrequencyCalculator {
    
    companion object {
        // فرکانس پایه A4 (440 Hz)
        private const val BASE_FREQUENCY_A4 = 440.0
        // شماره نت A4 در سیستم MIDI (69)
        private const val A4_MIDI_NUMBER = 69
        // نسبت استاندارد (2^(1/12) ≈ 1.059463)
        private const val STANDARD_RATIO = 1.059463094359295
    }
    
    // نسبت فاصله نت‌ها (قابل تنظیم از 0.001 تا 3.000)
    private var intervalRatio: Double = STANDARD_RATIO
    
    // تعداد اکتاوها بر اساس نسبت
    private var octaveCount: Int = 7
    
    /**
     * تنظیم نسبت فاصله نت‌ها
     */
    fun setIntervalRatio(ratio: Double) {
        intervalRatio = when {
            ratio < 0.001 -> 0.001
            ratio > 3.000 -> 3.000
            else -> ratio
        }
        calculateOctaveCount()
    }
    
    /**
     * دریافت نسبت فعلی
     */
    fun getIntervalRatio(): Double = intervalRatio
    
    /**
     * دریافت تعداد اکتاوها
     */
    fun getOctaveCount(): Int = octaveCount
    
    /**
     * محاسبه تعداد اکتاوها بر اساس نسبت
     * هر هفت نت متوالی یک اکتاو محسوب می‌شوند
     */
    private fun calculateOctaveCount() {
        // محاسبه بازه فرکانسی قابل شنیدن (20 Hz تا 20000 Hz)
        val minFreq = 20.0
        val maxFreq = 20000.0
        
        // محاسبه تعداد نت‌هایی که در این بازه قرار می‌گیرند
        var noteCount = 0
        var currentFreq = BASE_FREQUENCY_A4
        
        // شمارش نت‌های بالاتر از A4
        while (currentFreq <= maxFreq) {
            noteCount++
            currentFreq *= intervalRatio
        }
        
        // شمارش نت‌های پایین‌تر از A4
        currentFreq = BASE_FREQUENCY_A4 / intervalRatio
        while (currentFreq >= minFreq) {
            noteCount++
            currentFreq /= intervalRatio
        }
        
        // هر 7 نت یک اکتاو
        octaveCount = (noteCount / 7).coerceAtLeast(1)
    }
    
    /**
     * محاسبه فرکانس نت بر اساس شماره MIDI و نسبت تنظیم شده
     */
    fun calculateFrequency(midiNumber: Int): Double {
        val semitoneDistance = midiNumber - A4_MIDI_NUMBER
        return BASE_FREQUENCY_A4 * intervalRatio.pow(semitoneDistance.toDouble())
    }
    
    /**
     * محاسبه فرکانس نت بر اساس نام نت و اکتاو
     */
    fun calculateFrequency(noteName: String, octave: Int): Double {
        val midiNumber = getMidiNumber(noteName, octave)
        return calculateFrequency(midiNumber)
    }
    
    /**
     * تبدیل نام نت و اکتاو به شماره MIDI
     */
    private fun getMidiNumber(noteName: String, octave: Int): Int {
        val noteOffset = when (noteName.uppercase()) {
            "C" -> 0
            "C#", "DB" -> 1
            "D" -> 2
            "D#", "EB" -> 3
            "E" -> 4
            "F" -> 5
            "F#", "GB" -> 6
            "G" -> 7
            "G#", "AB" -> 8
            "A" -> 9
            "A#", "BB" -> 10
            "B" -> 11
            else -> 0
        }
        return (octave + 1) * 12 + noteOffset
    }
    
    /**
     * تبدیل شماره MIDI به نام نت
     */
    fun getMidiNoteName(midiNumber: Int): String {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val octave = (midiNumber / 12) - 1
        val noteIndex = midiNumber % 12
        return "${noteNames[noteIndex]}$octave"
    }
    
    /**
     * دریافت لیست تمام نت‌های قابل نمایش
     */
    fun getAllNotes(): List<PianoNote> {
        val notes = mutableListOf<PianoNote>()
        val startMidi = 21 // A0
        val endMidi = startMidi + (octaveCount * 12) // محدود به تعداد اکتاوهای محاسبه شده
        
        for (midiNumber in startMidi until endMidi) {
            val frequency = calculateFrequency(midiNumber)
            val noteName = getMidiNoteName(midiNumber)
            val isBlackKey = isBlackKey(midiNumber % 12)
            notes.add(PianoNote(midiNumber, noteName, frequency, isBlackKey))
        }
        
        return notes
    }
    
    /**
     * تشخیص اینکه آیا نت مشکی است یا سفید
     */
    private fun isBlackKey(noteIndex: Int): Boolean {
        return when (noteIndex) {
            1, 3, 6, 8, 10 -> true // C#, D#, F#, G#, A#
            else -> false
        }
    }
}

/**
 * کلاس داده برای نگهداری اطلاعات هر نت
 */
data class PianoNote(
    val midiNumber: Int,
    val name: String,
    val frequency: Double,
    val isBlackKey: Boolean
)

