package com.sd.pianopro.instruments

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

/**
 * مخزن سازها - مدیریت دریافت لیست سازها از سرور
 * ایده توسط S.D داده شده
 */
class InstrumentRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "InstrumentRepository"
        private const val BASE_URL = "https://api.pianopro.sd.com" // URL فرضی
        private const val INSTRUMENTS_ENDPOINT = "/instruments"
        private const val CATEGORIES_ENDPOINT = "/categories"
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    // Cache برای سازها
    private var cachedInstruments: List<Instrument>? = null
    private var lastFetchTime = 0L
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 دقیقه
    
    /**
     * دریافت لیست تمام سازها
     */
    suspend fun getInstruments(forceRefresh: Boolean = false): List<Instrument> {
        return withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            
            // بررسی cache
            if (!forceRefresh && cachedInstruments != null && 
                (currentTime - lastFetchTime) < cacheValidityDuration) {
                return@withContext cachedInstruments!!
            }
            
            try {
                val instruments = fetchInstrumentsFromServer()
                cachedInstruments = instruments
                lastFetchTime = currentTime
                instruments
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch instruments from server", e)
                // در صورت خطا، سازهای پیش‌فرض را برگردان
                getDefaultInstruments()
            }
        }
    }
    
    /**
     * دریافت سازها از سرور
     */
    private suspend fun fetchInstrumentsFromServer(): List<Instrument> {
        val request = Request.Builder()
            .url("$BASE_URL$INSTRUMENTS_ENDPOINT")
            .build()
        
        val response = okHttpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw IOException("Server returned error: ${response.code}")
        }
        
        val responseBody = response.body?.string() ?: throw IOException("Empty response body")
        
        val listType = object : TypeToken<List<Instrument>>() {}.type
        return gson.fromJson(responseBody, listType)
    }
    
    /**
     * دریافت سازهای پیش‌فرض (در صورت عدم دسترسی به سرور)
     */
    private fun getDefaultInstruments(): List<Instrument> {
        return listOf(
            Instrument(
                id = "piano_classic",
                name = "classic_piano",
                displayName = "پیانو کلاسیک",
                description = "صدای پیانو کلاسیک با کیفیت بالا",
                category = InstrumentCategory.PIANO,
                downloadUrl = "https://example.com/piano_classic.zip",
                fileSize = 50 * 1024 * 1024, // 50 MB
                version = "1.0",
                author = "S.D Audio",
                license = "Creative Commons",
                tags = listOf("کلاسیک", "آکوستیک", "پیانو")
            ),
            Instrument(
                id = "guitar_acoustic",
                name = "acoustic_guitar",
                displayName = "گیتار آکوستیک",
                description = "صدای گیتار آکوستیک طبیعی",
                category = InstrumentCategory.GUITAR,
                downloadUrl = "https://example.com/guitar_acoustic.zip",
                fileSize = 35 * 1024 * 1024, // 35 MB
                version = "1.0",
                author = "S.D Audio",
                license = "Creative Commons",
                tags = listOf("آکوستیک", "گیتار", "طبیعی")
            ),
            Instrument(
                id = "violin_classical",
                name = "classical_violin",
                displayName = "ویولن کلاسیک",
                description = "صدای ویولن کلاسیک با جزئیات بالا",
                category = InstrumentCategory.VIOLIN,
                downloadUrl = "https://example.com/violin_classical.zip",
                fileSize = 40 * 1024 * 1024, // 40 MB
                version = "1.0",
                author = "S.D Audio",
                license = "Creative Commons",
                tags = listOf("کلاسیک", "ویولن", "زهی")
            ),
            Instrument(
                id = "flute_concert",
                name = "concert_flute",
                displayName = "فلوت کنسرت",
                description = "صدای فلوت کنسرت حرفه‌ای",
                category = InstrumentCategory.FLUTE,
                downloadUrl = "https://example.com/flute_concert.zip",
                fileSize = 25 * 1024 * 1024, // 25 MB
                version = "1.0",
                author = "S.D Audio",
                license = "Creative Commons",
                tags = listOf("کنسرت", "فلوت", "بادی")
            ),
            Instrument(
                id = "synthesizer_modern",
                name = "modern_synth",
                displayName = "سینتی‌سایزر مدرن",
                description = "صداهای سینتی‌سایزر مدرن و الکترونیک",
                category = InstrumentCategory.SYNTHESIZER,
                downloadUrl = "https://example.com/synthesizer_modern.zip",
                fileSize = 60 * 1024 * 1024, // 60 MB
                version = "1.0",
                author = "S.D Audio",
                license = "Creative Commons",
                tags = listOf("مدرن", "الکترونیک", "سینتی")
            )
        )
    }
    
    /**
     * جستجو در سازها
     */
    suspend fun searchInstruments(query: String): List<Instrument> {
        val allInstruments = getInstruments()
        return allInstruments.filter { instrument ->
            instrument.displayName.contains(query, ignoreCase = true) ||
            instrument.description.contains(query, ignoreCase = true) ||
            instrument.tags.any { it.contains(query, ignoreCase = true) }
        }
    }
    
    /**
     * دریافت سازها بر اساس دسته‌بندی
     */
    suspend fun getInstrumentsByCategory(category: InstrumentCategory): List<Instrument> {
        val allInstruments = getInstruments()
        return allInstruments.filter { it.category == category }
    }
    
    /**
     * دریافت جزئیات ساز
     */
    suspend fun getInstrumentDetails(instrumentId: String): Instrument? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL$INSTRUMENTS_ENDPOINT/$instrumentId")
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let { gson.fromJson(it, Instrument::class.java) }
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch instrument details: $instrumentId", e)
                null
            }
        }
    }
    
    /**
     * دریافت دسته‌بندی‌های موجود
     */
    suspend fun getAvailableCategories(): List<InstrumentCategory> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL$CATEGORIES_ENDPOINT")
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val listType = object : TypeToken<List<String>>() {}.type
                    val categoryNames: List<String> = gson.fromJson(responseBody, listType)
                    categoryNames.mapNotNull { name ->
                        try {
                            InstrumentCategory.valueOf(name.uppercase())
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                } else {
                    InstrumentCategory.values().toList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch categories", e)
                InstrumentCategory.values().toList()
            }
        }
    }
    
    /**
     * پاک کردن cache
     */
    fun clearCache() {
        cachedInstruments = null
        lastFetchTime = 0L
    }
}

