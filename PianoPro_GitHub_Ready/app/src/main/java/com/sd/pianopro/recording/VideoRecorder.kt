package com.sd.pianopro.recording

import android.content.Context
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.Surface
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * کلاس ضبط ویدئو
 * ایده توسط S.D داده شده
 */
class VideoRecorder(private val context: Context) {
    
    companion object {
        private const val TAG = "VideoRecorder"
        private const val VIDEO_FORMAT = ".mp4"
    }
    
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var isRecording = false
    
    // Listener برای وضعیت ضبط
    private var onRecordingStateListener: ((Boolean, String?) -> Unit)? = null
    
    /**
     * تنظیم listener برای وضعیت ضبط
     */
    fun setOnRecordingStateListener(listener: (Boolean, String?) -> Unit) {
        onRecordingStateListener = listener
    }
    
    /**
     * شروع ضبط ویدئو
     */
    fun startRecording(previewSurface: Surface? = null): Boolean {
        if (isRecording) {
            Log.w(TAG, "Recording is already in progress")
            return false
        }
        
        try {
            // ایجاد فایل برای ذخیره ضبط
            currentRecordingFile = createVideoFile()
            
            // راه‌اندازی MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                // تنظیمات صدا
                setAudioSource(MediaRecorder.AudioSource.MIC)
                
                // تنظیمات ویدئو (اگر دوربین در دسترس باشد)
                if (previewSurface != null) {
                    setVideoSource(MediaRecorder.VideoSource.SURFACE)
                }
                
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                
                // تنظیمات کدک‌ها
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                if (previewSurface != null) {
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    setVideoSize(1920, 1080)
                    setVideoFrameRate(30)
                    setVideoEncodingBitRate(10000000)
                }
                
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(currentRecordingFile?.absolutePath)
                
                prepare()
                start()
            }
            
            isRecording = true
            onRecordingStateListener?.invoke(true, currentRecordingFile?.absolutePath)
            Log.d(TAG, "Video recording started: ${currentRecordingFile?.absolutePath}")
            return true
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start video recording", e)
            cleanup()
            onRecordingStateListener?.invoke(false, null)
            return false
        }
    }
    
    /**
     * شروع ضبط صرفاً صدا (بدون ویدئو)
     */
    fun startAudioOnlyRecording(): Boolean {
        return startRecording(null)
    }
    
    /**
     * توقف ضبط ویدئو
     */
    fun stopRecording(): String? {
        if (!isRecording) {
            Log.w(TAG, "No recording in progress")
            return null
        }
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            
            isRecording = false
            val filePath = currentRecordingFile?.absolutePath
            onRecordingStateListener?.invoke(false, filePath)
            Log.d(TAG, "Video recording stopped: $filePath")
            
            cleanup()
            return filePath
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop video recording", e)
            cleanup()
            return null
        }
    }
    
    /**
     * ایجاد فایل ویدئویی جدید
     */
    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PIANO_VIDEO_$timeStamp$VIDEO_FORMAT"
        
        // ایجاد پوشه برای ذخیره فایل‌ها
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "PianoPro")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, fileName)
    }
    
    /**
     * پاک‌سازی منابع
     */
    private fun cleanup() {
        mediaRecorder?.release()
        mediaRecorder = null
    }
    
    /**
     * بررسی وضعیت ضبط
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * دریافت فایل ضبط فعلی
     */
    fun getCurrentRecordingFile(): File? = currentRecordingFile
    
    /**
     * دریافت لیست تمام فایل‌های ضبط شده
     */
    fun getRecordedFiles(): List<File> {
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "PianoPro")
        return if (storageDir.exists()) {
            storageDir.listFiles { file -> file.name.endsWith(VIDEO_FORMAT) }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * حذف فایل ضبط شده
     */
    fun deleteRecording(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete recording: $filePath", e)
            false
        }
    }
    
    /**
     * دریافت Surface برای ضبط ویدئو
     */
    fun getRecorderSurface(): Surface? {
        return mediaRecorder?.surface
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        if (isRecording) {
            stopRecording()
        }
        cleanup()
    }
}

