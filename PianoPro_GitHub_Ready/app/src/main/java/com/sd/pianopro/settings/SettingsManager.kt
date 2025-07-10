package com.sd.pianopro.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * مدیریت تنظیمات برنامه
 * ایده توسط S.D داده شده
 */
class SettingsManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "piano_pro_settings"
        private const val KEY_INTERVAL_RATIO = "interval_ratio"
        private const val KEY_SHOW_FREQUENCY = "show_frequency"
        private const val KEY_HORIZONTAL_LAYOUT = "horizontal_layout"
        private const val KEY_DUAL_VIEW = "dual_view"
        private const val KEY_MASTER_VOLUME = "master_volume"
        private const val KEY_CURRENT_INSTRUMENT = "current_instrument"
        private const val KEY_KEY_SIZE_SCALE = "key_size_scale"
        
        // مقادیر پیش‌فرض
        private const val DEFAULT_INTERVAL_RATIO = 1.059463094359295 // نسبت استاندارد
        private const val DEFAULT_SHOW_FREQUENCY = true
        private const val DEFAULT_HORIZONTAL_LAYOUT = false
        private const val DEFAULT_DUAL_VIEW = false
        private const val DEFAULT_MASTER_VOLUME = 0.7f
        private const val DEFAULT_CURRENT_INSTRUMENT = "piano"
        private const val DEFAULT_KEY_SIZE_SCALE = 1.0f
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * ذخیره نسبت فاصله نت‌ها
     */
    fun setIntervalRatio(ratio: Double) {
        sharedPreferences.edit()
            .putFloat(KEY_INTERVAL_RATIO, ratio.toFloat())
            .apply()
    }
    
    /**
     * دریافت نسبت فاصله نت‌ها
     */
    fun getIntervalRatio(): Double {
        return sharedPreferences.getFloat(KEY_INTERVAL_RATIO, DEFAULT_INTERVAL_RATIO.toFloat()).toDouble()
    }
    
    /**
     * ذخیره وضعیت نمایش فرکانس
     */
    fun setShowFrequency(show: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_SHOW_FREQUENCY, show)
            .apply()
    }
    
    /**
     * دریافت وضعیت نمایش فرکانس
     */
    fun getShowFrequency(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_FREQUENCY, DEFAULT_SHOW_FREQUENCY)
    }
    
    /**
     * ذخیره وضعیت نمایش افقی
     */
    fun setHorizontalLayout(horizontal: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_HORIZONTAL_LAYOUT, horizontal)
            .apply()
    }
    
    /**
     * دریافت وضعیت نمایش افقی
     */
    fun getHorizontalLayout(): Boolean {
        return sharedPreferences.getBoolean(KEY_HORIZONTAL_LAYOUT, DEFAULT_HORIZONTAL_LAYOUT)
    }
    
    /**
     * ذخیره وضعیت نمایش دوگانه
     */
    fun setDualView(dual: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_DUAL_VIEW, dual)
            .apply()
    }
    
    /**
     * دریافت وضعیت نمایش دوگانه
     */
    fun getDualView(): Boolean {
        return sharedPreferences.getBoolean(KEY_DUAL_VIEW, DEFAULT_DUAL_VIEW)
    }
    
    /**
     * ذخیره حجم صدا
     */
    fun setMasterVolume(volume: Float) {
        sharedPreferences.edit()
            .putFloat(KEY_MASTER_VOLUME, volume)
            .apply()
    }
    
    /**
     * دریافت حجم صدا
     */
    fun getMasterVolume(): Float {
        return sharedPreferences.getFloat(KEY_MASTER_VOLUME, DEFAULT_MASTER_VOLUME)
    }
    
    /**
     * ذخیره ساز فعلی
     */
    fun setCurrentInstrument(instrument: String) {
        sharedPreferences.edit()
            .putString(KEY_CURRENT_INSTRUMENT, instrument)
            .apply()
    }
    
    /**
     * دریافت ساز فعلی
     */
    fun getCurrentInstrument(): String {
        return sharedPreferences.getString(KEY_CURRENT_INSTRUMENT, DEFAULT_CURRENT_INSTRUMENT) ?: DEFAULT_CURRENT_INSTRUMENT
    }
    
    /**
     * ذخیره مقیاس اندازه کلیدها
     */
    fun setKeySizeScale(scale: Float) {
        sharedPreferences.edit()
            .putFloat(KEY_KEY_SIZE_SCALE, scale)
            .apply()
    }
    
    /**
     * دریافت مقیاس اندازه کلیدها
     */
    fun getKeySizeScale(): Float {
        return sharedPreferences.getFloat(KEY_KEY_SIZE_SCALE, DEFAULT_KEY_SIZE_SCALE)
    }
    
    /**
     * بازنشانی تمام تنظیمات به مقادیر پیش‌فرض
     */
    fun resetToDefaults() {
        sharedPreferences.edit()
            .putFloat(KEY_INTERVAL_RATIO, DEFAULT_INTERVAL_RATIO.toFloat())
            .putBoolean(KEY_SHOW_FREQUENCY, DEFAULT_SHOW_FREQUENCY)
            .putBoolean(KEY_HORIZONTAL_LAYOUT, DEFAULT_HORIZONTAL_LAYOUT)
            .putBoolean(KEY_DUAL_VIEW, DEFAULT_DUAL_VIEW)
            .putFloat(KEY_MASTER_VOLUME, DEFAULT_MASTER_VOLUME)
            .putString(KEY_CURRENT_INSTRUMENT, DEFAULT_CURRENT_INSTRUMENT)
            .putFloat(KEY_KEY_SIZE_SCALE, DEFAULT_KEY_SIZE_SCALE)
            .apply()
    }
    
    /**
     * دریافت تمام تنظیمات به صورت Map
     */
    fun getAllSettings(): Map<String, Any> {
        return mapOf(
            KEY_INTERVAL_RATIO to getIntervalRatio(),
            KEY_SHOW_FREQUENCY to getShowFrequency(),
            KEY_HORIZONTAL_LAYOUT to getHorizontalLayout(),
            KEY_DUAL_VIEW to getDualView(),
            KEY_MASTER_VOLUME to getMasterVolume(),
            KEY_CURRENT_INSTRUMENT to getCurrentInstrument(),
            KEY_KEY_SIZE_SCALE to getKeySizeScale()
        )
    }
}

