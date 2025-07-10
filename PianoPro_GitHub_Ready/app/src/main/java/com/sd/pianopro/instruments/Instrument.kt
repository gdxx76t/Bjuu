package com.sd.pianopro.instruments

/**
 * کلاس داده برای نگهداری اطلاعات ساز
 * ایده توسط S.D داده شده
 */
data class Instrument(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val category: InstrumentCategory,
    val downloadUrl: String,
    val fileSize: Long,
    val version: String,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val previewUrl: String? = null,
    val thumbnailUrl: String? = null,
    val author: String? = null,
    val license: String? = null,
    val tags: List<String> = emptyList()
)

/**
 * دسته‌بندی سازها
 */
enum class InstrumentCategory(val displayName: String) {
    PIANO("پیانو"),
    GUITAR("گیتار"),
    VIOLIN("ویولن"),
    FLUTE("فلوت"),
    TRUMPET("ترومپت"),
    DRUMS("درام"),
    SYNTHESIZER("سینتی‌سایزر"),
    ORGAN("ارگ"),
    SAXOPHONE("ساکسوفون"),
    CELLO("ویولن‌سل"),
    HARP("چنگ"),
    TRADITIONAL("سنتی"),
    ELECTRONIC("الکترونیک"),
    OTHER("سایر")
}

/**
 * وضعیت دانلود ساز
 */
enum class DownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
    PAUSED
}

/**
 * اطلاعات پیشرفت دانلود
 */
data class DownloadProgress(
    val instrumentId: String,
    val status: DownloadStatus,
    val progress: Int, // درصد (0-100)
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speed: Long, // بایت در ثانیه
    val remainingTime: Long // ثانیه
)

