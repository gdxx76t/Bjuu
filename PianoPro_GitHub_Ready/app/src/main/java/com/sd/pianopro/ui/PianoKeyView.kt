package com.sd.pianopro.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.sd.pianopro.R
import com.sd.pianopro.audio.PianoNote

/**
 * نمای سفارشی برای هر کلید پیانو
 * ایده توسط S.D داده شده
 */
class PianoKeyView @JvmOverloads constructor(
    context: Context,
    val note: PianoNote,
    private val pianoView: PianoView,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // رنگ‌ها
    private val whiteKeyColor = ContextCompat.getColor(context, R.color.white_key)
    private val whiteKeyPressedColor = ContextCompat.getColor(context, R.color.white_key_pressed)
    private val blackKeyColor = ContextCompat.getColor(context, R.color.black_key)
    private val blackKeyPressedColor = ContextCompat.getColor(context, R.color.black_key_pressed)
    private val keyBorderColor = ContextCompat.getColor(context, R.color.key_border)
    
    private var isPressed = false
    private var cornerRadius = 8f
    
    init {
        setupPaints()
        isClickable = true
        isFocusable = true
    }
    
    /**
     * تنظیم paint ها
     */
    private fun setupPaints() {
        // Paint اصلی برای رنگ کلید
        paint.style = Paint.Style.FILL
        
        // Paint برای متن
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        
        // Paint برای حاشیه
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 2f
        borderPaint.color = keyBorderColor
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        
        // تعیین رنگ بر اساس نوع کلید و وضعیت فشردن
        val keyColor = when {
            isPressed && note.isBlackKey -> blackKeyPressedColor
            isPressed && !note.isBlackKey -> whiteKeyPressedColor
            note.isBlackKey -> blackKeyColor
            else -> whiteKeyColor
        }
        
        paint.color = keyColor
        
        // رسم کلید با گوشه‌های گرد
        val rect = RectF(0f, 0f, width, height)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        
        // رسم حاشیه
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
        
        // رسم نام نت
        drawNoteName(canvas, width, height)
        
        // اگر کلید فشرده شده، افکت نوری اضافه کن
        if (isPressed) {
            drawPressEffect(canvas, width, height)
        }
    }
    
    /**
     * رسم نام نت
     */
    private fun drawNoteName(canvas: Canvas, width: Float, height: Float) {
        // تنظیم رنگ متن
        textPaint.color = if (note.isBlackKey) Color.WHITE else Color.BLACK
        
        // تنظیم اندازه متن بر اساس اندازه کلید
        textPaint.textSize = minOf(width, height) * 0.15f
        
        // محاسبه موقعیت متن (پایین کلید)
        val textX = width / 2f
        val textY = height - (height * 0.1f)
        
        // رسم نام نت
        val noteName = note.name.replace(Regex("\\d"), "") // حذف شماره اکتاو برای نمایش بهتر
        canvas.drawText(noteName, textX, textY, textPaint)
        
        // رسم شماره اکتاو (کوچک‌تر)
        val octaveNumber = note.name.replace(Regex("[^\\d]"), "")
        if (octaveNumber.isNotEmpty()) {
            textPaint.textSize *= 0.7f
            canvas.drawText(octaveNumber, textX, textY - (textPaint.textSize * 1.2f), textPaint)
        }
    }
    
    /**
     * رسم افکت فشردن
     */
    private fun drawPressEffect(canvas: Canvas, width: Float, height: Float) {
        val effectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        effectPaint.style = Paint.Style.FILL
        effectPaint.color = Color.WHITE
        effectPaint.alpha = 30 // شفافیت کم برای افکت ملایم
        
        val rect = RectF(0f, 0f, width, height)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, effectPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                pianoView.playNote(note.midiNumber)
                invalidate()
                return true
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                pianoView.stopNote(note.midiNumber)
                invalidate()
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                // بررسی اینکه آیا انگشت هنوز روی کلید است
                val x = event.x
                val y = event.y
                val isInBounds = x >= 0 && x <= width && y >= 0 && y <= height
                
                if (isPressed && !isInBounds) {
                    // انگشت از کلید خارج شده
                    isPressed = false
                    pianoView.stopNote(note.midiNumber)
                    invalidate()
                } else if (!isPressed && isInBounds) {
                    // انگشت دوباره روی کلید آمده
                    isPressed = true
                    pianoView.playNote(note.midiNumber)
                    invalidate()
                }
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    /**
     * تنظیم وضعیت فشردن از خارج (برای کیبورد خارجی)
     */
    fun setPressed(pressed: Boolean) {
        if (isPressed != pressed) {
            isPressed = pressed
            if (pressed) {
                pianoView.playNote(note.midiNumber)
            } else {
                pianoView.stopNote(note.midiNumber)
            }
            invalidate()
        }
    }
    
    /**
     * دریافت وضعیت فشردن
     */
    fun isKeyPressed(): Boolean = isPressed
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // اطمینان از اینکه کلید حداقل اندازه مشخصی دارد
        val minWidth = (40 * resources.displayMetrics.density).toInt()
        val minHeight = (120 * resources.displayMetrics.density).toInt()
        
        val width = maxOf(MeasureSpec.getSize(widthMeasureSpec), minWidth)
        val height = maxOf(MeasureSpec.getSize(heightMeasureSpec), minHeight)
        
        setMeasuredDimension(width, height)
    }
}

