package com.sd.pianopro.instruments

import android.content.Context
import android.util.Log
import com.sd.pianopro.audio.AudioEngine
import kotlinx.coroutines.*

/**
 * مدیر کلی سازها - ترکیب repository و downloader
 * ایده توسط S.D داده شده
 */
class InstrumentManager(
    private val context: Context,
    private val audioEngine: AudioEngine
) {
    
    companion object {
        private const val TAG = "InstrumentManager"
    }
    
    private val repository = InstrumentRepository(context)
    private val downloader = InstrumentDownloader(context)
    
    private var currentInstrument: Instrument? = null
    private val downloadedInstruments = mutableListOf<Instrument>()
    
    // Listener ها
    private var onInstrumentChangeListener: ((Instrument?) -> Unit)? = null
    private var onDownloadProgressListener: ((DownloadProgress) -> Unit)? = null
    private var onInstrumentListUpdateListener: ((List<Instrument>) -> Unit)? = null
    
    init {
        // تنظیم listener برای پیشرفت دانلود
        downloader.setOnDownloadProgressListener { progress ->
            onDownloadProgressListener?.invoke(progress)
            
            // اگر دانلود کامل شد، ساز را به لیست اضافه کن
            if (progress.status == DownloadStatus.DOWNLOADED) {
                updateDownloadedInstruments()
            }
        }
        
        // بارگذاری سازهای دانلود شده
        loadDownloadedInstruments()
    }
    
    /**
     * دریافت لیست تمام سازهای موجود
     */
    suspend fun getAvailableInstruments(forceRefresh: Boolean = false): List<Instrument> {
        return repository.getInstruments(forceRefresh)
    }
    
    /**
     * دریافت لیست سازهای دانلود شده
     */
    fun getDownloadedInstruments(): List<Instrument> {
        return downloadedInstruments.toList()
    }
    
    /**
     * جستجو در سازها
     */
    suspend fun searchInstruments(query: String): List<Instrument> {
        return repository.searchInstruments(query)
    }
    
    /**
     * دریافت سازها بر اساس دسته‌بندی
     */
    suspend fun getInstrumentsByCategory(category: InstrumentCategory): List<Instrument> {
        return repository.getInstrumentsByCategory(category)
    }
    
    /**
     * شروع دانلود ساز
     */
    fun downloadInstrument(instrument: Instrument): Boolean {
        return downloader.downloadInstrument(instrument)
    }
    
    /**
     * لغو دانلود ساز
     */
    fun cancelDownload(instrumentId: String): Boolean {
        return downloader.cancelDownload(instrumentId)
    }
    
    /**
     * حذف ساز دانلود شده
     */
    fun deleteInstrument(instrument: Instrument): Boolean {
        val success = downloader.deleteInstrument(instrument)
        if (success) {
            downloadedInstruments.removeAll { it.id == instrument.id }
            
            // اگر ساز فعلی حذف شد، به ساز پیش‌فرض برگرد
            if (currentInstrument?.id == instrument.id) {
                setCurrentInstrument(null)
            }
            
            onInstrumentListUpdateListener?.invoke(downloadedInstruments)
        }
        return success
    }
    
    /**
     * تنظیم ساز فعلی
     */
    fun setCurrentInstrument(instrument: Instrument?) {
        currentInstrument = instrument
        
        // بارگذاری صداهای ساز در AudioEngine
        if (instrument != null && instrument.isDownloaded) {
            loadInstrumentSounds(instrument)
        }
        
        onInstrumentChangeListener?.invoke(instrument)
        Log.d(TAG, "Current instrument changed to: ${instrument?.displayName ?: "Default"}")
    }
    
    /**
     * دریافت ساز فعلی
     */
    fun getCurrentInstrument(): Instrument? = currentInstrument
    
    /**
     * بارگذاری صداهای ساز در AudioEngine
     */
    private fun loadInstrumentSounds(instrument: Instrument) {
        try {
            // TODO: پیاده‌سازی بارگذاری فایل‌های صوتی ساز
            // این باید فایل‌های zip را استخراج کرده و صداها را بارگذاری کند
            Log.d(TAG, "Loading sounds for instrument: ${instrument.displayName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load instrument sounds: ${instrument.id}", e)
        }
    }
    
    /**
     * بارگذاری لیست سازهای دانلود شده
     */
    private fun loadDownloadedInstruments() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TODO: اسکن پوشه instruments و شناسایی سازهای دانلود شده
                // در حال حاضر لیست خالی برمی‌گرداند
                downloadedInstruments.clear()
                
                withContext(Dispatchers.Main) {
                    onInstrumentListUpdateListener?.invoke(downloadedInstruments)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load downloaded instruments", e)
            }
        }
    }
    
    /**
     * به‌روزرسانی لیست سازهای دانلود شده
     */
    private fun updateDownloadedInstruments() {
        loadDownloadedInstruments()
    }
    
    /**
     * بررسی اینکه آیا ساز دانلود شده است
     */
    fun isInstrumentDownloaded(instrumentId: String): Boolean {
        return downloadedInstruments.any { it.id == instrumentId }
    }
    
    /**
     * دریافت پیشرفت دانلود ساز
     */
    fun getDownloadProgress(instrumentId: String): DownloadProgress? {
        return downloader.getDownloadProgress(instrumentId)
    }
    
    /**
     * دریافت اندازه کل فایل‌های دانلود شده
     */
    fun getTotalDownloadedSize(): Long {
        return downloader.getTotalDownloadedSize()
    }
    
    /**
     * دریافت دسته‌بندی‌های موجود
     */
    suspend fun getAvailableCategories(): List<InstrumentCategory> {
        return repository.getAvailableCategories()
    }
    
    /**
     * تنظیم listener برای تغییر ساز
     */
    fun setOnInstrumentChangeListener(listener: (Instrument?) -> Unit) {
        onInstrumentChangeListener = listener
    }
    
    /**
     * تنظیم listener برای پیشرفت دانلود
     */
    fun setOnDownloadProgressListener(listener: (DownloadProgress) -> Unit) {
        onDownloadProgressListener = listener
    }
    
    /**
     * تنظیم listener برای به‌روزرسانی لیست سازها
     */
    fun setOnInstrumentListUpdateListener(listener: (List<Instrument>) -> Unit) {
        onInstrumentListUpdateListener = listener
    }
    
    /**
     * پاک کردن cache
     */
    fun clearCache() {
        repository.clearCache()
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        downloader.release()
    }
}

