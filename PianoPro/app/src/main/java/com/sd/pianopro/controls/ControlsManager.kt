package com.sd.pianopro.controls

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import com.sd.pianopro.audio.AudioEngine
import com.sd.pianopro.ui.PianoView

/**
 * مدیریت کنترل‌های اضافی برنامه
 * ایده توسط S.D داده شده
 */
class ControlsManager(
    private val context: Context,
    private val audioEngine: AudioEngine,
    private val pianoView: PianoView
) {
    
    // نقشه کلیدهای کیبورد به نت‌های MIDI
    private val keyboardMapping = mapOf(
        KeyEvent.KEYCODE_A to 60, // C4
        KeyEvent.KEYCODE_W to 61, // C#4
        KeyEvent.KEYCODE_S to 62, // D4
        KeyEvent.KEYCODE_E to 63, // D#4
        KeyEvent.KEYCODE_D to 64, // E4
        KeyEvent.KEYCODE_F to 65, // F4
        KeyEvent.KEYCODE_T to 66, // F#4
        KeyEvent.KEYCODE_G to 67, // G4
        KeyEvent.KEYCODE_Y to 68, // G#4
        KeyEvent.KEYCODE_H to 69, // A4
        KeyEvent.KEYCODE_U to 70, // A#4
        KeyEvent.KEYCODE_J to 71, // B4
        KeyEvent.KEYCODE_K to 72, // C5
        KeyEvent.KEYCODE_O to 73, // C#5
        KeyEvent.KEYCODE_L to 74, // D5
        KeyEvent.KEYCODE_P to 75, // D#5
        KeyEvent.KEYCODE_SEMICOLON to 76, // E5
        
        // اکتاو پایین‌تر
        KeyEvent.KEYCODE_Z to 48, // C3
        KeyEvent.KEYCODE_S to 49, // C#3
        KeyEvent.KEYCODE_X to 50, // D3
        KeyEvent.KEYCODE_D to 51, // D#3
        KeyEvent.KEYCODE_C to 52, // E3
        KeyEvent.KEYCODE_V to 53, // F3
        KeyEvent.KEYCODE_G to 54, // F#3
        KeyEvent.KEYCODE_B to 55, // G3
        KeyEvent.KEYCODE_H to 56, // G#3
        KeyEvent.KEYCODE_N to 57, // A3
        KeyEvent.KEYCODE_J to 58, // A#3
        KeyEvent.KEYCODE_M to 59  // B3
    )
    
    private val pressedKeys = mutableSetOf<Int>()
    
    /**
     * مدیریت رویدادهای کیبورد
     */
    fun handleKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        val midiNote = keyboardMapping[keyCode] ?: return false
        
        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (!pressedKeys.contains(keyCode)) {
                    pressedKeys.add(keyCode)
                    audioEngine.playNote(midiNote)
                    return true
                }
            }
            KeyEvent.ACTION_UP -> {
                if (pressedKeys.contains(keyCode)) {
                    pressedKeys.remove(keyCode)
                    audioEngine.stopNote(midiNote)
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * مدیریت رویدادهای لمسی پیشرفته
     */
    fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // شروع لمس
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // حرکت انگشت - می‌توان برای slide بین نت‌ها استفاده کرد
                return true
            }
            MotionEvent.ACTION_UP -> {
                // پایان لمس
                return true
            }
        }
        return false
    }
    
    /**
     * کنترل حجم صدا با کلیدهای حجم
     */
    fun handleVolumeKeys(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                val currentVolume = getCurrentVolume()
                val newVolume = (currentVolume + 0.1f).coerceAtMost(1.0f)
                audioEngine.setMasterVolume(newVolume)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val currentVolume = getCurrentVolume()
                val newVolume = (currentVolume - 0.1f).coerceAtLeast(0.0f)
                audioEngine.setMasterVolume(newVolume)
                return true
            }
        }
        return false
    }
    
    /**
     * کنترل‌های میانبر کیبورد
     */
    fun handleShortcutKeys(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isCtrlPressed) {
            when (keyCode) {
                KeyEvent.KEYCODE_R -> {
                    // شروع/توقف ضبط
                    return true
                }
                KeyEvent.KEYCODE_P -> {
                    // پخش/مکث
                    return true
                }
                KeyEvent.KEYCODE_SPACE -> {
                    // توقف تمام نت‌ها
                    audioEngine.stopAllNotes()
                    return true
                }
                KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_EQUALS -> {
                    // افزایش اندازه کلیدها
                    pianoView.setKeySize(1.2f)
                    return true
                }
                KeyEvent.KEYCODE_MINUS -> {
                    // کاهش اندازه کلیدها
                    pianoView.setKeySize(0.8f)
                    return true
                }
            }
        }
        return false
    }
    
    /**
     * دریافت حجم فعلی
     */
    private fun getCurrentVolume(): Float {
        // باید از AudioEngine یا SettingsManager دریافت شود
        return 0.7f // مقدار پیش‌فرض
    }
    
    /**
     * تنظیم حساسیت لمس
     */
    fun setTouchSensitivity(sensitivity: Float) {
        // پیاده‌سازی تنظیم حساسیت لمس
    }
    
    /**
     * فعال/غیرفعال کردن کنترل‌های کیبورد
     */
    fun setKeyboardControlsEnabled(enabled: Boolean) {
        // پیاده‌سازی فعال/غیرفعال کردن کنترل‌های کیبورد
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        pressedKeys.clear()
    }
}

