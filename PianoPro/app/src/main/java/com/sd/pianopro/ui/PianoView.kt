package com.sd.pianopro.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.sd.pianopro.R
import com.sd.pianopro.audio.AudioEngine
import com.sd.pianopro.audio.PianoNote
import kotlin.math.*

/**
 * نمای سفارشی برای نمایش کلیدهای پیانو
 * ایده توسط S.D داده شده
 */
class PianoView @JvmOverloads constructor(
    context: Context,
    private val audioEngine: AudioEngine,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {
    
    companion object {
        private const val MIN_KEY_WIDTH = 40f
        private const val MAX_KEY_WIDTH = 120f
        private const val DEFAULT_KEY_WIDTH = 60f
        private const val WHITE_KEY_HEIGHT_RATIO = 1f
        private const val BLACK_KEY_HEIGHT_RATIO = 0.6f
        private const val BLACK_KEY_WIDTH_RATIO = 0.7f
    }
    
    private var pianoContainer: LinearLayout
    private var keyWidth = DEFAULT_KEY_WIDTH
    private var keyHeight = 200f
    private var isHorizontalLayout = false
    private var isDualView = false
    private var scaleFactor = 1f
    
    // رنگ‌ها
    private val whiteKeyColor = ContextCompat.getColor(context, R.color.white_key)
    private val whiteKeyPressedColor = ContextCompat.getColor(context, R.color.white_key_pressed)
    private val blackKeyColor = ContextCompat.getColor(context, R.color.black_key)
    private val blackKeyPressedColor = ContextCompat.getColor(context, R.color.black_key_pressed)
    private val keyBorderColor = ContextCompat.getColor(context, R.color.key_border)
    
    // لیست نت‌ها و کلیدها
    private var notes: List<PianoNote> = emptyList()
    private val keyViews = mutableListOf<PianoKeyView>()
    private val pressedKeys = mutableSetOf<Int>()
    
    // Listener برای نواختن نت‌ها
    private var onNotePlayListener: ((Int, Double) -> Unit)? = null
    
    // Gesture detector برای zoom
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())
    
    init {
        // ایجاد container اصلی
        pianoContainer = LinearLayout(context)
        pianoContainer.orientation = LinearLayout.HORIZONTAL
        addView(pianoContainer)
        
        // بارگذاری نت‌ها و ایجاد کلیدها
        loadNotesAndCreateKeys()
        
        // تنظیم scroll
        isHorizontalScrollBarEnabled = true
        isSmoothScrollingEnabled = true
    }
    
    /**
     * بارگذاری نت‌ها از AudioEngine و ایجاد کلیدها
     */
    private fun loadNotesAndCreateKeys() {
        notes = audioEngine.getAllNotes()
        createKeyViews()
    }
    
    /**
     * ایجاد نماهای کلیدها
     */
    private fun createKeyViews() {
        pianoContainer.removeAllViews()
        keyViews.clear()
        
        if (isDualView) {
            createDualViewLayout()
        } else {
            createSingleViewLayout()
        }
    }
    
    /**
     * ایجاد layout تک نمایه
     */
    private fun createSingleViewLayout() {
        val whiteKeys = mutableListOf<PianoKeyView>()
        val blackKeys = mutableListOf<PianoKeyView>()
        
        // ایجاد کلیدهای سفید و سیاه
        notes.forEach { note ->
            val keyView = PianoKeyView(context, note, this)
            keyViews.add(keyView)
            
            if (note.isBlackKey) {
                blackKeys.add(keyView)
            } else {
                whiteKeys.add(keyView)
            }
        }
        
        if (isHorizontalLayout) {
            createHorizontalLayout(whiteKeys, blackKeys)
        } else {
            createVerticalLayout(whiteKeys, blackKeys)
        }
    }
    
    /**
     * ایجاد layout عمودی (استاندارد)
     */
    private fun createVerticalLayout(whiteKeys: List<PianoKeyView>, blackKeys: List<PianoKeyView>) {
        val frameLayout = FrameLayout(context)
        
        // اضافه کردن کلیدهای سفید
        val whiteKeyContainer = LinearLayout(context)
        whiteKeyContainer.orientation = LinearLayout.HORIZONTAL
        
        whiteKeys.forEach { keyView ->
            keyView.layoutParams = LinearLayout.LayoutParams(
                (keyWidth * scaleFactor).toInt(),
                (keyHeight * scaleFactor).toInt()
            ).apply {
                setMargins(1, 1, 1, 1)
            }
            whiteKeyContainer.addView(keyView)
        }
        
        frameLayout.addView(whiteKeyContainer)
        
        // اضافه کردن کلیدهای سیاه
        val blackKeyContainer = LinearLayout(context)
        blackKeyContainer.orientation = LinearLayout.HORIZONTAL
        
        var whiteKeyIndex = 0
        blackKeys.forEach { keyView ->
            // محاسبه موقعیت کلید سیاه نسبت به کلیدهای سفید
            val position = calculateBlackKeyPosition(keyView.note.midiNumber, whiteKeyIndex)
            
            keyView.layoutParams = LinearLayout.LayoutParams(
                (keyWidth * BLACK_KEY_WIDTH_RATIO * scaleFactor).toInt(),
                (keyHeight * BLACK_KEY_HEIGHT_RATIO * scaleFactor).toInt()
            ).apply {
                leftMargin = (position * keyWidth * scaleFactor).toInt()
            }
            blackKeyContainer.addView(keyView)
            
            if (isWhiteKey(keyView.note.midiNumber + 1)) {
                whiteKeyIndex++
            }
        }
        
        frameLayout.addView(blackKeyContainer)
        pianoContainer.addView(frameLayout)
    }
    
    /**
     * ایجاد layout افقی
     */
    private fun createHorizontalLayout(whiteKeys: List<PianoKeyView>, blackKeys: List<PianoKeyView>) {
        // در layout افقی، کلیدها به صورت عمودی چیده می‌شوند
        val verticalContainer = LinearLayout(context)
        verticalContainer.orientation = LinearLayout.VERTICAL
        
        (whiteKeys + blackKeys).sortedBy { it.note.midiNumber }.forEach { keyView ->
            keyView.layoutParams = LinearLayout.LayoutParams(
                (keyHeight * scaleFactor).toInt(), // عرض و ارتفاع جابجا می‌شوند
                (keyWidth * scaleFactor).toInt()
            ).apply {
                setMargins(1, 1, 1, 1)
            }
            verticalContainer.addView(keyView)
        }
        
        pianoContainer.addView(verticalContainer)
    }
    
    /**
     * ایجاد layout دوگانه (دو قسمت از پیانو)
     */
    private fun createDualViewLayout() {
        val dualContainer = LinearLayout(context)
        dualContainer.orientation = if (isHorizontalLayout) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        
        val midPoint = notes.size / 2
        val firstHalf = notes.subList(0, midPoint)
        val secondHalf = notes.subList(midPoint, notes.size)
        
        // ایجاد دو نیمه
        createHalfPiano(firstHalf, dualContainer)
        createHalfPiano(secondHalf, dualContainer)
        
        pianoContainer.addView(dualContainer)
    }
    
    /**
     * ایجاد نیمی از پیانو برای نمایش دوگانه
     */
    private fun createHalfPiano(notesList: List<PianoNote>, parent: LinearLayout) {
        val halfContainer = LinearLayout(context)
        halfContainer.orientation = if (isHorizontalLayout) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        
        notesList.forEach { note ->
            val keyView = PianoKeyView(context, note, this)
            keyViews.add(keyView)
            
            val width = if (isHorizontalLayout) (keyHeight * scaleFactor).toInt() else (keyWidth * scaleFactor).toInt()
            val height = if (isHorizontalLayout) (keyWidth * scaleFactor).toInt() else (keyHeight * scaleFactor).toInt()
            
            keyView.layoutParams = LinearLayout.LayoutParams(width, height).apply {
                setMargins(1, 1, 1, 1)
            }
            halfContainer.addView(keyView)
        }
        
        parent.addView(halfContainer)
    }
    
    /**
     * محاسبه موقعیت کلید سیاه
     */
    private fun calculateBlackKeyPosition(midiNumber: Int, whiteKeyIndex: Int): Float {
        val noteInOctave = midiNumber % 12
        return when (noteInOctave) {
            1 -> whiteKeyIndex + 0.7f // C#
            3 -> whiteKeyIndex + 1.3f // D#
            6 -> whiteKeyIndex + 2.7f // F#
            8 -> whiteKeyIndex + 3.3f // G#
            10 -> whiteKeyIndex + 4.3f // A#
            else -> whiteKeyIndex.toFloat()
        }
    }
    
    /**
     * تشخیص اینکه آیا نت سفید است
     */
    private fun isWhiteKey(midiNumber: Int): Boolean {
        val noteInOctave = midiNumber % 12
        return noteInOctave in listOf(0, 2, 4, 5, 7, 9, 11) // C, D, E, F, G, A, B
    }
    
    /**
     * تنظیم layout افقی
     */
    fun setHorizontalLayout(horizontal: Boolean) {
        if (isHorizontalLayout != horizontal) {
            isHorizontalLayout = horizontal
            createKeyViews()
        }
    }
    
    /**
     * تنظیم نمایش دوگانه
     */
    fun setDualView(dual: Boolean) {
        if (isDualView != dual) {
            isDualView = dual
            createKeyViews()
        }
    }
    
    /**
     * تنظیم اندازه کلیدها
     */
    fun setKeySize(scale: Float) {
        scaleFactor = scale.coerceIn(0.5f, 2f)
        createKeyViews()
    }
    
    /**
     * تنظیم listener برای نواختن نت‌ها
     */
    fun setOnNotePlayListener(listener: (Int, Double) -> Unit) {
        onNotePlayListener = listener
    }
    
    /**
     * نواختن نت
     */
    internal fun playNote(midiNumber: Int) {
        audioEngine.playNote(midiNumber)
        val frequency = audioEngine.getNoteFrequency(midiNumber)
        onNotePlayListener?.invoke(midiNumber, frequency)
        
        pressedKeys.add(midiNumber)
        invalidate()
    }
    
    /**
     * توقف نت
     */
    internal fun stopNote(midiNumber: Int) {
        audioEngine.stopNote(midiNumber)
        pressedKeys.remove(midiNumber)
        invalidate()
    }
    
    /**
     * بررسی اینکه آیا کلید فشرده شده است
     */
    internal fun isKeyPressed(midiNumber: Int): Boolean = pressedKeys.contains(midiNumber)
    
    /**
     * مدیریت touch events برای zoom
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
    
    /**
     * Listener برای zoom
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            setKeySize(scaleFactor * scale)
            return true
        }
    }
    
    /**
     * Listener برای gesture ها
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // بازگشت به اندازه پیش‌فرض با double tap
            setKeySize(1f)
            return true
        }
    }
    
    /**
     * به‌روزرسانی نت‌ها (برای تغییر نسبت فاصله)
     */
    fun updateNotes() {
        loadNotesAndCreateKeys()
    }
}

