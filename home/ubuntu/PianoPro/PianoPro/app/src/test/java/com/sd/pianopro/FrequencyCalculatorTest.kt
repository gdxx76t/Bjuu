package com.sd.pianopro

import com.sd.pianopro.audio.FrequencyCalculator
import org.junit.Test
import org.junit.Assert.*

/**
 * تست واحد برای کلاس FrequencyCalculator
 * ایده توسط S.D داده شده
 */
class FrequencyCalculatorTest {
    
    @Test
    fun testStandardTuning() {
        val calculator = FrequencyCalculator()
        
        // تست فرکانس A4 (440 Hz)
        val a4Frequency = calculator.getNoteFrequency(69) // A4 = MIDI note 69
        assertEquals(440.0, a4Frequency, 0.1)
        
        // تست فرکانس C4 (Middle C)
        val c4Frequency = calculator.getNoteFrequency(60) // C4 = MIDI note 60
        assertEquals(261.63, c4Frequency, 0.1)
    }
    
    @Test
    fun testCustomIntervalRatio() {
        val calculator = FrequencyCalculator()
        calculator.setIntervalRatio(1.5) // نسبت 1.5
        
        // با نسبت 1.5، فاصله بین نت‌ها بیشتر می‌شود
        val c4Frequency = calculator.getNoteFrequency(60)
        val d4Frequency = calculator.getNoteFrequency(62)
        
        // بررسی اینکه نسبت درست محاسبه شده
        assertTrue(d4Frequency > c4Frequency)
    }
    
    @Test
    fun testOctaveCalculation() {
        val calculator = FrequencyCalculator()
        
        // تست محاسبه تعداد اکتاوها
        calculator.setIntervalRatio(1.0)
        var octaveCount = calculator.calculateOctaveCount()
        assertTrue(octaveCount > 0)
        
        calculator.setIntervalRatio(2.0)
        octaveCount = calculator.calculateOctaveCount()
        assertTrue(octaveCount > 0)
    }
    
    @Test
    fun testNoteNames() {
        val calculator = FrequencyCalculator()
        
        // تست نام‌های نت‌ها
        assertEquals("C4", calculator.getNoteName(60))
        assertEquals("A4", calculator.getNoteName(69))
        assertEquals("C5", calculator.getNoteName(72))
    }
    
    @Test
    fun testFrequencyRange() {
        val calculator = FrequencyCalculator()
        
        // تست محدوده فرکانس‌ها
        for (midiNote in 21..108) { // محدوده پیانو
            val frequency = calculator.getNoteFrequency(midiNote)
            assertTrue("Frequency should be positive", frequency > 0)
            assertTrue("Frequency should be reasonable", frequency < 20000) // زیر 20 کیلوهرتز
        }
    }
}

