package com.sd.pianopro.instruments

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * مدیریت دانلود سازهای مختلف
 * ایده توسط S.D داده شده
 */
class InstrumentDownloader(private val context: Context) {
    
    companion object {
        private const val TAG = "InstrumentDownloader"
        private const val INSTRUMENTS_DIR = "instruments"
        private const val CHUNK_SIZE = 8192
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private val activeDownloads = ConcurrentHashMap<String, Job>()
    private val downloadProgress = ConcurrentHashMap<String, DownloadProgress>()
    
    // Listener برای پیشرفت دانلود
    private var onDownloadProgressListener: ((DownloadProgress) -> Unit)? = null
    
    /**
     * تنظیم listener برای پیشرفت دانلود
     */
    fun setOnDownloadProgressListener(listener: (DownloadProgress) -> Unit) {
        onDownloadProgressListener = listener
    }
    
    /**
     * شروع دانلود ساز
     */
    fun downloadInstrument(instrument: Instrument): Boolean {
        if (activeDownloads.containsKey(instrument.id)) {
            Log.w(TAG, "Download already in progress for instrument: ${instrument.id}")
            return false
        }
        
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                downloadInstrumentInternal(instrument)
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for instrument: ${instrument.id}", e)
                updateDownloadProgress(instrument.id, DownloadStatus.FAILED, 0, 0, 0)
            } finally {
                activeDownloads.remove(instrument.id)
            }
        }
        
        activeDownloads[instrument.id] = job
        return true
    }
    
    /**
     * پیاده‌سازی داخلی دانلود
     */
    private suspend fun downloadInstrumentInternal(instrument: Instrument) {
        val request = Request.Builder()
            .url(instrument.downloadUrl)
            .build()
        
        val response = okHttpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw IOException("Download failed with code: ${response.code}")
        }
        
        val responseBody = response.body ?: throw IOException("Response body is null")
        val totalBytes = responseBody.contentLength()
        
        // ایجاد فایل مقصد
        val destinationFile = createInstrumentFile(instrument)
        val outputStream = FileOutputStream(destinationFile)
        
        val inputStream = responseBody.byteStream()
        val buffer = ByteArray(CHUNK_SIZE)
        var downloadedBytes = 0L
        var lastUpdateTime = System.currentTimeMillis()
        var lastDownloadedBytes = 0L
        
        updateDownloadProgress(instrument.id, DownloadStatus.DOWNLOADING, 0, 0, totalBytes)
        
        try {
            while (true) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) break
                
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead
                
                // به‌روزرسانی پیشرفت
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= 1000) { // هر ثانیه
                    val progress = if (totalBytes > 0) {
                        ((downloadedBytes * 100) / totalBytes).toInt()
                    } else 0
                    
                    val speed = if (currentTime > lastUpdateTime) {
                        ((downloadedBytes - lastDownloadedBytes) * 1000) / (currentTime - lastUpdateTime)
                    } else 0
                    
                    val remainingTime = if (speed > 0) {
                        (totalBytes - downloadedBytes) / speed
                    } else 0
                    
                    updateDownloadProgress(
                        instrument.id,
                        DownloadStatus.DOWNLOADING,
                        progress,
                        downloadedBytes,
                        totalBytes,
                        speed,
                        remainingTime
                    )
                    
                    lastUpdateTime = currentTime
                    lastDownloadedBytes = downloadedBytes
                }
                
                // بررسی لغو دانلود
                if (!activeDownloads.containsKey(instrument.id)) {
                    throw InterruptedException("Download cancelled")
                }
            }
            
            outputStream.flush()
            updateDownloadProgress(instrument.id, DownloadStatus.DOWNLOADED, 100, downloadedBytes, totalBytes)
            Log.d(TAG, "Download completed for instrument: ${instrument.id}")
            
        } finally {
            inputStream.close()
            outputStream.close()
            response.close()
        }
    }
    
    /**
     * به‌روزرسانی پیشرفت دانلود
     */
    private fun updateDownloadProgress(
        instrumentId: String,
        status: DownloadStatus,
        progress: Int,
        downloadedBytes: Long,
        totalBytes: Long,
        speed: Long = 0,
        remainingTime: Long = 0
    ) {
        val progressInfo = DownloadProgress(
            instrumentId = instrumentId,
            status = status,
            progress = progress,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            speed = speed,
            remainingTime = remainingTime
        )
        
        downloadProgress[instrumentId] = progressInfo
        onDownloadProgressListener?.invoke(progressInfo)
    }
    
    /**
     * ایجاد فایل برای ذخیره ساز
     */
    private fun createInstrumentFile(instrument: Instrument): File {
        val instrumentsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), INSTRUMENTS_DIR)
        if (!instrumentsDir.exists()) {
            instrumentsDir.mkdirs()
        }
        
        val categoryDir = File(instrumentsDir, instrument.category.name.lowercase())
        if (!categoryDir.exists()) {
            categoryDir.mkdirs()
        }
        
        val fileName = "${instrument.id}_${instrument.version}.zip"
        return File(categoryDir, fileName)
    }
    
    /**
     * لغو دانلود ساز
     */
    fun cancelDownload(instrumentId: String): Boolean {
        val job = activeDownloads[instrumentId]
        return if (job != null) {
            job.cancel()
            activeDownloads.remove(instrumentId)
            updateDownloadProgress(instrumentId, DownloadStatus.PAUSED, 0, 0, 0)
            true
        } else {
            false
        }
    }
    
    /**
     * مکث دانلود ساز
     */
    fun pauseDownload(instrumentId: String): Boolean {
        return cancelDownload(instrumentId) // در حال حاضر مشابه لغو
    }
    
    /**
     * ادامه دانلود ساز
     */
    fun resumeDownload(instrument: Instrument): Boolean {
        // در حال حاضر شروع مجدد دانلود
        return downloadInstrument(instrument)
    }
    
    /**
     * بررسی وضعیت دانلود
     */
    fun getDownloadProgress(instrumentId: String): DownloadProgress? {
        return downloadProgress[instrumentId]
    }
    
    /**
     * بررسی اینکه آیا ساز در حال دانلود است
     */
    fun isDownloading(instrumentId: String): Boolean {
        return activeDownloads.containsKey(instrumentId)
    }
    
    /**
     * حذف ساز دانلود شده
     */
    fun deleteInstrument(instrument: Instrument): Boolean {
        return try {
            val file = File(instrument.localPath ?: return false)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete instrument: ${instrument.id}", e)
            false
        }
    }
    
    /**
     * دریافت اندازه کل فایل‌های دانلود شده
     */
    fun getTotalDownloadedSize(): Long {
        val instrumentsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), INSTRUMENTS_DIR)
        return if (instrumentsDir.exists()) {
            calculateDirectorySize(instrumentsDir)
        } else {
            0L
        }
    }
    
    /**
     * محاسبه اندازه پوشه
     */
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        activeDownloads.values.forEach { it.cancel() }
        activeDownloads.clear()
        downloadProgress.clear()
    }
}

