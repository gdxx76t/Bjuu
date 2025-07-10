package com.sd.pianopro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sd.pianopro.audio.AudioEngine
import com.sd.pianopro.ui.PianoView
import com.sd.pianopro.settings.SettingsManager
import com.sd.pianopro.recording.AudioRecorder
import com.sd.pianopro.recording.VideoRecorder
import com.sd.pianopro.recording.PlaybackManager
import com.sd.pianopro.midi.MidiManager
import com.sd.pianopro.controls.ControlsManager
import com.sd.pianopro.instruments.InstrumentManager
import java.io.File
import java.util.*

/**
 * اکتیویتی اصلی برنامه پیانو
 * ایده توسط S.D داده شده
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }
    
    private lateinit var audioEngine: AudioEngine
    private lateinit var pianoView: PianoView
    private lateinit var settingsManager: SettingsManager
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var videoRecorder: VideoRecorder
    private lateinit var playbackManager: PlaybackManager
    private lateinit var midiManager: MidiManager
    private lateinit var controlsManager: ControlsManager
    private lateinit var instrumentManager: InstrumentManager
    
    // UI Components
    private lateinit var intervalRatioSeekBar: SeekBar
    private lateinit var intervalRatioText: TextView
    private lateinit var octaveCountText: TextView
    private lateinit var frequencyDisplay: TextView
    private lateinit var showFrequencySwitch: Switch
    private lateinit var horizontalLayoutSwitch: Switch
    private lateinit var dualViewSwitch: Switch
    private lateinit var recordAudioButton: Button
    private lateinit var recordVideoButton: Button
    private lateinit var pianoContainer: FrameLayout
    
    private var currentFrequency = 0.0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // بررسی مجوزها
        checkPermissions()
        
        // راه‌اندازی مدیر تنظیمات
        settingsManager = SettingsManager(this)
        
        // راه‌اندازی موتور صوتی
        audioEngine = AudioEngine(this)
        audioEngine.loadPianoSounds()
        
        // راه‌اندازی مدیران ضبط و پخش
        audioRecorder = AudioRecorder(this)
        videoRecorder = VideoRecorder(this)
        playbackManager = PlaybackManager(this)
        
        // راه‌اندازی مدیر MIDI
        midiManager = MidiManager(this, audioEngine)
        
        // راه‌اندازی مدیر سازها
        instrumentManager = InstrumentManager(this, audioEngine)
        
        // تنظیم listener ها برای ضبط
        setupRecordingListeners()
        
        // بارگذاری تنظیمات ذخیره شده
        loadSavedSettings()
        
        // راه‌اندازی UI
        initializeUI()
        setupEventListeners()
        
        // ایجاد نمای پیانو
        createPianoView()
        
        // راه‌اندازی مدیر کنترل‌ها (باید بعد از ایجاد pianoView باشد)
        controlsManager = ControlsManager(this, audioEngine, pianoView)
        
        // به‌روزرسانی اولیه UI
        updateUI()
    }
    
    /**
     * بررسی و درخواست مجوزهای لازم
     */
    private fun checkPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * بارگذاری تنظیمات ذخیره شده
     */
    private fun loadSavedSettings() {
        // بارگذاری نسبت فاصله
        val savedRatio = settingsManager.getIntervalRatio()
        audioEngine.setIntervalRatio(savedRatio)
        
        // بارگذاری حجم صدا
        val savedVolume = settingsManager.getMasterVolume()
        audioEngine.setMasterVolume(savedVolume)
    }
    
    /**
     * تنظیم listener ها برای ضبط
     */
    private fun setupRecordingListeners() {
        audioRecorder.setOnRecordingStateListener { isRecording, filePath ->
            runOnUiThread {
                if (isRecording) {
                    recordAudioButton.text = getString(R.string.stop_recording)
                    Toast.makeText(this, getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
                } else {
                    recordAudioButton.text = getString(R.string.record_audio)
                    if (filePath != null) {
                        Toast.makeText(this, "فایل ذخیره شد: ${File(filePath).name}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        
        videoRecorder.setOnRecordingStateListener { isRecording, filePath ->
            runOnUiThread {
                if (isRecording) {
                    recordVideoButton.text = getString(R.string.stop_recording)
                    Toast.makeText(this, getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
                } else {
                    recordVideoButton.text = getString(R.string.record_video)
                    if (filePath != null) {
                        Toast.makeText(this, "فایل ذخیره شد: ${File(filePath).name}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        
        playbackManager.setOnPlaybackStateListener { isPlaying, filePath ->
            runOnUiThread {
                // می‌توان UI مربوط به پخش را در اینجا به‌روزرسانی کرد
            }
        }
    }
    
    /**
     * راه‌اندازی اجزای UI
     */
    private fun initializeUI() {
        intervalRatioSeekBar = findViewById(R.id.intervalRatioSeekBar)
        intervalRatioText = findViewById(R.id.intervalRatioText)
        octaveCountText = findViewById(R.id.octaveCountText)
        frequencyDisplay = findViewById(R.id.frequencyDisplay)
        showFrequencySwitch = findViewById(R.id.showFrequencySwitch)
        horizontalLayoutSwitch = findViewById(R.id.horizontalLayoutSwitch)
        dualViewSwitch = findViewById(R.id.dualViewSwitch)
        recordAudioButton = findViewById(R.id.recordAudioButton)
        recordVideoButton = findViewById(R.id.recordVideoButton)
        pianoContainer = findViewById(R.id.pianoContainer)
    }
    
    /**
     * تنظیم event listener ها
     */
    private fun setupEventListeners() {
        // SeekBar برای تنظیم نسبت فاصله
        intervalRatioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // تبدیل progress (0-2999) به نسبت (0.001-3.000)
                    val ratio = 0.001 + (progress / 1000.0)
                    audioEngine.setIntervalRatio(ratio)
                    settingsManager.setIntervalRatio(ratio) // ذخیره تنظیمات
                    updateUI()
                    recreatePianoView()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Switch برای نمایش فرکانس
        showFrequencySwitch.setOnCheckedChangeListener { _, isChecked ->
            frequencyDisplay.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
            settingsManager.setShowFrequency(isChecked) // ذخیره تنظیمات
        }
        
        // Switch برای نمایش افقی
        horizontalLayoutSwitch.setOnCheckedChangeListener { _, isChecked ->
            pianoView.setHorizontalLayout(isChecked)
            settingsManager.setHorizontalLayout(isChecked) // ذخیره تنظیمات
        }
        
        // Switch برای نمایش دوگانه
        dualViewSwitch.setOnCheckedChangeListener { _, isChecked ->
            pianoView.setDualView(isChecked)
            settingsManager.setDualView(isChecked) // ذخیره تنظیمات
        }
        
        // دکمه ضبط صدا
        recordAudioButton.setOnClickListener {
            toggleAudioRecording()
        }
        
        // دکمه ضبط ویدئو
        recordVideoButton.setOnClickListener {
            toggleVideoRecording()
        }
    }
    
    /**
     * ایجاد نمای پیانو
     */
    private fun createPianoView() {
        pianoView = PianoView(this, audioEngine)
        pianoView.setOnNotePlayListener { midiNumber, frequency ->
            currentFrequency = frequency
            updateFrequencyDisplay()
        }
        
        pianoContainer.removeAllViews()
        pianoContainer.addView(pianoView)
    }
    
    /**
     * بازسازی نمای پیانو (برای تغییر تعداد اکتاوها)
     */
    private fun recreatePianoView() {
        pianoView.updateNotes()
    }
    
    /**
     * به‌روزرسانی UI
     */
    private fun updateUI() {
        val ratio = audioEngine.getIntervalRatio()
        val octaveCount = audioEngine.getOctaveCount()
        
        // به‌روزرسانی متن نسبت
        intervalRatioText.text = getString(R.string.interval_ratio, ratio)
        
        // به‌روزرسانی متن تعداد اکتاو
        octaveCountText.text = getString(R.string.octave_count, octaveCount)
        
        // به‌روزرسانی SeekBar
        val progress = ((ratio - 0.001) * 1000).toInt()
        intervalRatioSeekBar.progress = progress
        
        // بارگذاری تنظیمات ذخیره شده برای UI
        showFrequencySwitch.isChecked = settingsManager.getShowFrequency()
        horizontalLayoutSwitch.isChecked = settingsManager.getHorizontalLayout()
        dualViewSwitch.isChecked = settingsManager.getDualView()
        
        // تنظیم visibility فرکانس
        frequencyDisplay.visibility = if (settingsManager.getShowFrequency()) 
            android.view.View.VISIBLE else android.view.View.GONE
        
        updateFrequencyDisplay()
    }
    
    /**
     * به‌روزرسانی نمایش فرکانس
     */
    private fun updateFrequencyDisplay() {
        if (showFrequencySwitch.isChecked) {
            frequencyDisplay.text = getString(R.string.frequency_display, currentFrequency)
        }
    }
    
    /**
     * تغییر وضعیت ضبط صدا
     */
    private fun toggleAudioRecording() {
        if (audioRecorder.isRecording()) {
            stopAudioRecording()
        } else {
            startAudioRecording()
        }
    }
    
    /**
     * شروع ضبط صدا
     */
    private fun startAudioRecording() {
        audioRecorder.startRecording()
    }
    
    /**
     * توقف ضبط صدا
     */
    private fun stopAudioRecording() {
        audioRecorder.stopRecording()
    }
    
    /**
     * تغییر وضعیت ضبط ویدئو
     */
    private fun toggleVideoRecording() {
        if (videoRecorder.isRecording()) {
            stopVideoRecording()
        } else {
            startVideoRecording()
        }
    }
    
    /**
     * شروع ضبط ویدئو
     */
    private fun startVideoRecording() {
        // ضبط صرفاً صدا (بدون تصویر دوربین)
        videoRecorder.startAudioOnlyRecording()
    }
    
    /**
     * توقف ضبط ویدئو
     */
    private fun stopVideoRecording() {
        videoRecorder.stopRecording()
    }
    
    /**
     * پاسخ به درخواست مجوزها
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }
            
            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.permission_required),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // مدیریت کلیدهای کیبورد برای نواختن نت‌ها
        if (controlsManager.handleKeyEvent(keyCode, event)) {
            return true
        }
        
        // مدیریت کلیدهای حجم
        if (controlsManager.handleVolumeKeys(keyCode)) {
            return true
        }
        
        // مدیریت میانبرهای کیبورد
        if (controlsManager.handleShortcutKeys(keyCode, event)) {
            return true
        }
        
        return super.onKeyDown(keyCode, event)
    }
    
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (controlsManager.handleKeyEvent(keyCode, event)) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
        audioRecorder.release()
        videoRecorder.release()
        playbackManager.release()
        midiManager.release()
        controlsManager.release()
        instrumentManager.release()
    }
    
    override fun onPause() {
        super.onPause()
        audioEngine.stopAllNotes()
    }
}

