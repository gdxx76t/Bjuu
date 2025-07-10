package com.sd.pianopro.recording

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import java.io.File

/**
 * مدیریت پخش فایل‌های ضبط شده
 * ایده توسط S.D داده شده
 */
class PlaybackManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PlaybackManager"
    }
    
    private var audioPlayer: MediaPlayer? = null
    private var videoView: VideoView? = null
    private var isPlaying = false
    private var currentFilePath: String? = null
    
    // Listener برای وضعیت پخش
    private var onPlaybackStateListener: ((Boolean, String?) -> Unit)? = null
    
    /**
     * تنظیم listener برای وضعیت پخش
     */
    fun setOnPlaybackStateListener(listener: (Boolean, String?) -> Unit) {
        onPlaybackStateListener = listener
    }
    
    /**
     * تنظیم VideoView برای پخش ویدئو
     */
    fun setVideoView(videoView: VideoView) {
        this.videoView = videoView
    }
    
    /**
     * پخش فایل صوتی
     */
    fun playAudio(filePath: String): Boolean {
        if (isPlaying) {
            stopPlayback()
        }
        
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "Audio file does not exist: $filePath")
                return false
            }
            
            audioPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnCompletionListener {
                    stopPlayback()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    stopPlayback()
                    false
                }
                prepare()
                start()
            }
            
            isPlaying = true
            currentFilePath = filePath
            onPlaybackStateListener?.invoke(true, filePath)
            Log.d(TAG, "Audio playback started: $filePath")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio: $filePath", e)
            cleanup()
            return false
        }
    }
    
    /**
     * پخش فایل ویدئویی
     */
    fun playVideo(filePath: String): Boolean {
        if (videoView == null) {
            Log.e(TAG, "VideoView not set")
            return false
        }
        
        if (isPlaying) {
            stopPlayback()
        }
        
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "Video file does not exist: $filePath")
                return false
            }
            
            val uri = Uri.fromFile(file)
            videoView?.apply {
                setVideoURI(uri)
                setOnCompletionListener {
                    stopPlayback()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "VideoView error: what=$what, extra=$extra")
                    stopPlayback()
                    false
                }
                start()
            }
            
            isPlaying = true
            currentFilePath = filePath
            onPlaybackStateListener?.invoke(true, filePath)
            Log.d(TAG, "Video playback started: $filePath")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play video: $filePath", e)
            return false
        }
    }
    
    /**
     * توقف پخش
     */
    fun stopPlayback() {
        try {
            audioPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            
            videoView?.apply {
                if (isPlaying) {
                    stopPlayback()
                }
            }
            
            isPlaying = false
            val filePath = currentFilePath
            currentFilePath = null
            onPlaybackStateListener?.invoke(false, filePath)
            Log.d(TAG, "Playback stopped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        } finally {
            cleanup()
        }
    }
    
    /**
     * مکث پخش
     */
    fun pausePlayback() {
        try {
            audioPlayer?.pause()
            videoView?.pause()
            Log.d(TAG, "Playback paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback", e)
        }
    }
    
    /**
     * ادامه پخش
     */
    fun resumePlayback() {
        try {
            audioPlayer?.start()
            videoView?.start()
            Log.d(TAG, "Playback resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming playback", e)
        }
    }
    
    /**
     * بررسی وضعیت پخش
     */
    fun isPlaying(): Boolean = isPlaying
    
    /**
     * دریافت فایل در حال پخش
     */
    fun getCurrentFile(): String? = currentFilePath
    
    /**
     * دریافت موقعیت فعلی پخش (میلی‌ثانیه)
     */
    fun getCurrentPosition(): Int {
        return try {
            audioPlayer?.currentPosition ?: videoView?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * دریافت مدت زمان کل فایل (میلی‌ثانیه)
     */
    fun getDuration(): Int {
        return try {
            audioPlayer?.duration ?: videoView?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * تنظیم موقعیت پخش
     */
    fun seekTo(position: Int) {
        try {
            audioPlayer?.seekTo(position)
            videoView?.seekTo(position)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking to position: $position", e)
        }
    }
    
    /**
     * پاک‌سازی منابع
     */
    private fun cleanup() {
        audioPlayer?.release()
        audioPlayer = null
    }
    
    /**
     * آزادسازی منابع
     */
    fun release() {
        stopPlayback()
        cleanup()
    }
}

