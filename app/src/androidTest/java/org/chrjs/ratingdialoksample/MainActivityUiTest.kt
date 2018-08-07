package org.chrjs.ratingdialoksample

import android.content.SharedPreferences
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.text.format.DateUtils
import android.view.View
import junit.framework.Assert.*
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@Suppress("RedundantVisibilityModifier")
@RunWith(AndroidJUnit4::class)
public class MainActivityUiTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java, true, false)

    private val preferences: SharedPreferences by lazy {
        InstrumentationRegistry.getTargetContext().getSharedPreferences("ratingDialok", 0)
    }

    private val preferencesEditor: SharedPreferences.Editor by lazy {
        preferences.edit()
    }

    companion object {
        private const val KEY_USER_HAS_RATED = "RD_KEY_USER_HAS_RATED"
        private const val KEY_NEVER_REMIND_AGAIN = "RD_KEY_NEVER_REMIND_AGAIN"
        private const val KEY_FIRST_START_DATE = "RD_KEY_FIRST_START_DATE"
        private const val KEY_LAUNCH_COUNT = "RD_KEY_LAUNCH_COUNT"
    }

    private fun clearSharedPrefs() {
        preferencesEditor.clear().apply()
    }

    private fun setConditionsAreMet() {
        preferencesEditor.putInt(KEY_LAUNCH_COUNT, 10).apply()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -10)
        preferencesEditor.putLong(KEY_FIRST_START_DATE, cal.timeInMillis).apply()
    }

    @Test
    fun resetShouldResetAllTheData() {
        preferencesEditor.putInt(KEY_LAUNCH_COUNT, 2)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -3)
        preferencesEditor.putLong(KEY_FIRST_START_DATE, cal.timeInMillis)
        preferencesEditor.putBoolean(KEY_USER_HAS_RATED, true)
        preferencesEditor.putBoolean(KEY_NEVER_REMIND_AGAIN, true)
        preferencesEditor.apply()

        activityTestRule.launchActivity(null)

        onView(withId(R.id.buttonReset)).perform(click())

        assertFalse(preferences.getBoolean(KEY_USER_HAS_RATED, true))
        assertFalse(preferences.getBoolean(KEY_NEVER_REMIND_AGAIN, true))
        assertEquals(preferences.getLong(KEY_FIRST_START_DATE, 123L), 0L)
        assertEquals(preferences.getInt(KEY_LAUNCH_COUNT, 123), 0)
    }

    @Test
    fun userSetRemindNeverAgainShouldBeSaved() {
        clearSharedPrefs()
        setConditionsAreMet()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button2), withText(R.string.remindNever))).perform(click())

        assertTrue(preferences.getBoolean(KEY_NEVER_REMIND_AGAIN, false))

        //Verify action callback
        assertTrue(activityTestRule.activity.remindNeverClicked)
    }

    @Test
    fun userSetRemindLaterShouldBeSaved() {
        clearSharedPrefs()
        setConditionsAreMet()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button3), withText(R.string.remindLater))).perform(click())

        val launchCount = preferences.getInt(KEY_LAUNCH_COUNT, 100)
        assertEquals(launchCount, 0)

        val firstLaunchDate = preferences.getLong(KEY_FIRST_START_DATE, 100)
        assertTrue(DateUtils.isToday(firstLaunchDate))

        //Verify action callback
        assertTrue(activityTestRule.activity.remindLaterClicked)
    }

    @Test
    fun dialogShouldPopupWhenRequirementsAreMet() {
        clearSharedPrefs()

        setConditionsAreMet()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow))).check(matches(isDisplayed()))
    }

    @Test
    fun dialogShouldNotPopupWhenUserAlreadyRated() {
        clearSharedPrefs()

        setConditionsAreMet()
        preferencesEditor.putBoolean(KEY_USER_HAS_RATED, true).commit()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow))).check(doesNotExist())
    }

    @Test
    fun dialogShouldNotPopupWhenUserSetNeverRemindAgain() {
        clearSharedPrefs()

        setConditionsAreMet()
        preferencesEditor.putBoolean(KEY_NEVER_REMIND_AGAIN, true).commit()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow))).check(doesNotExist())
    }

    @Test
    fun dialogShouldNotPopupWhenRequirementsAreNotMet() {
        clearSharedPrefs()

        preferencesEditor.putInt(KEY_LAUNCH_COUNT, 1).commit()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        preferencesEditor.putLong(KEY_FIRST_START_DATE, cal.timeInMillis).commit()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow))).check(doesNotExist())
    }

    @Test
    fun buttonVisibilityShouldAdjust() {
        clearSharedPrefs()

        activityTestRule.launchActivity(null)
        //All Buttons are visible
        val buttonDefault = onView(allOf<View>(withId(R.id.buttonDefaultDialog), isDisplayed()))
        buttonDefault.perform(click())

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button2), withText(R.string.remindNever), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button3), withText(R.string.remindLater), isDisplayed())).perform(click())

        //No remind never button
        onView(allOf<View>(withId(R.id.buttonNoRemindNever), isDisplayed())).perform(click())
        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button2))).check(matches(not(isDisplayed())))
        onView(allOf<View>(withId(android.R.id.button3), withText(R.string.remindLater), isDisplayed()))
                .check(matches(isDisplayed())).perform(click())

        //No remind later button
        onView(allOf<View>(withId(R.id.buttonNoRemindLater), isDisplayed())).perform(click())
        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button3))).check(matches(not(isDisplayed())))
        onView(allOf<View>(withId(android.R.id.button2), withText(R.string.remindNever), isDisplayed()))
                .check(matches(isDisplayed())).perform(click())
    }
}