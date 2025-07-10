package com.sd.pianopro

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * تست یکپارچه‌سازی برای MainActivity
 * ایده توسط S.D داده شده
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testUIElementsAreDisplayed() {
        // بررسی نمایش عناصر اصلی UI
        onView(withId(R.id.intervalRatioSeekBar)).check(matches(isDisplayed()))
        onView(withId(R.id.intervalRatioText)).check(matches(isDisplayed()))
        onView(withId(R.id.octaveCountText)).check(matches(isDisplayed()))
        onView(withId(R.id.showFrequencySwitch)).check(matches(isDisplayed()))
        onView(withId(R.id.recordAudioButton)).check(matches(isDisplayed()))
        onView(withId(R.id.recordVideoButton)).check(matches(isDisplayed()))
    }
    
    @Test
    fun testFrequencyDisplayToggle() {
        // تست فعال/غیرفعال کردن نمایش فرکانس
        onView(withId(R.id.showFrequencySwitch)).perform(click())
        onView(withId(R.id.frequencyDisplay)).check(matches(isDisplayed()))
        
        onView(withId(R.id.showFrequencySwitch)).perform(click())
        // فرکانس باید مخفی شود
    }
    
    @Test
    fun testRecordingButtons() {
        // تست دکمه‌های ضبط
        onView(withId(R.id.recordAudioButton))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.record_audio)))
        
        onView(withId(R.id.recordVideoButton))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.record_video)))
    }
    
    @Test
    fun testPianoViewIsDisplayed() {
        // بررسی نمایش نمای پیانو
        onView(withId(R.id.pianoContainer)).check(matches(isDisplayed()))
    }
    
    @Test
    fun testLayoutSwitches() {
        // تست سوئیچ‌های تغییر layout
        onView(withId(R.id.horizontalLayoutSwitch)).check(matches(isDisplayed()))
        onView(withId(R.id.dualViewSwitch)).check(matches(isDisplayed()))
        
        // تست تغییر به نمایش افقی
        onView(withId(R.id.horizontalLayoutSwitch)).perform(click())
        
        // تست تغییر به نمایش دوگانه
        onView(withId(R.id.dualViewSwitch)).perform(click())
    }
}

