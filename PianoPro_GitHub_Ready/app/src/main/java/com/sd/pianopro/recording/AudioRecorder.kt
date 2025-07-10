package com.sd.pianopro.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * کلاس ضبط صدا
 * ایده توسط S.D داده شده
 */
class AudioRecorder(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioRecorder"
        private const val AUDIO_FORMAT = ".m4a"
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
     * شروع ضبط صدا
     */
    fun startRecording(): Boolean {
        if (isRecording) {
            Log.w(TAG, "Recording is already in progress")
            return false
        }
        
        try {
            // ایجاد فایل برای ذخیره ضبط
            currentRecordingFile = createAudioFile()
            
            // راه‌اندازی MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(currentRecordingFile?.absolutePath)
                
                prepare()
                start()
            }
            
            isRecording = true
            onRecordingStateListener?.invoke(true, currentRecordingFile?.absolutePath)
            Log.d(TAG, "Audio recording started: ${currentRecordingFile?.absolutePath}")
            return true
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start audio recording", e)
            cleanup()
            onRecordingStateListener?.invoke(false, null)
            return false
        }
    }
    
    /**
     * توقف ضبط صدا
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
            Log.d(TAG, "Audio recording stopped: $filePath")
            
            cleanup()
            return filePath
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop audio recording", e)
            cleanup()
            return null
        }
    }
    
    /**
     * ایجاد فایل صوتی جدید
     */
    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PIANO_AUDIO_$timeStamp$AUDIO_FORMAT"
        
        // ایجاد پوشه برای ذخیره فایل‌ها
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "PianoPro")
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
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "PianoPro")
        return if (storageDir.exists()) {
            storageDir.listFiles { file -> file.name.endsWith(AUDIO_FORMAT) }?.toList() ?: emptyList()
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
     * آزادسازی منابع
     */
    fun release() {
        if (isRecording) {
            stopRecording()
        }
        cleanup()
    }
}

