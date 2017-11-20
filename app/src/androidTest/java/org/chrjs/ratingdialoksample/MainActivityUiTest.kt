package org.chrjs.ratingdialoksample

import android.content.SharedPreferences
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
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
    var activityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java, true, false)

    private var preferencesEditor: SharedPreferences.Editor? = null

    companion object {
        private val KEY_USER_HAS_RATED = "RD_KEY_USER_HAS_RATED"
        private val KEY_NEVER_REMIND_AGAIN = "RD_KEY_NEVER_REMIND_AGAIN"
        private val KEY_FIRST_START_DATE = "RD_KEY_FIRST_START_DATE"
        private val KEY_LAUNCH_COUNT = "RD_KEY_LAUNCH_COUNT"
    }

    private fun setupAndClearSharedPrefs() {
        preferencesEditor = InstrumentationRegistry.getTargetContext().getSharedPreferences("ratingDialok", 0).edit()
        preferencesEditor?.clear()!!.commit()
    }

    @Test
    fun dialogShouldPopupWhenRequirementsAreMet() {
        setupAndClearSharedPrefs()

        preferencesEditor!!.putInt(KEY_LAUNCH_COUNT, 10).commit()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -10)
        preferencesEditor!!.putLong(KEY_FIRST_START_DATE, cal.timeInMillis).commit()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow))).check(matches(isDisplayed()))
    }

    @Test
    fun dialogShouldNotPopupWhenUserAlreadyRated() {
        setupAndClearSharedPrefs()

        preferencesEditor!!.putBoolean(KEY_USER_HAS_RATED, true).commit()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow))).check(doesNotExist())
    }

    @Test
    fun dialogShouldNotPopupWhenUserSetNeverRemindAgain() {
        setupAndClearSharedPrefs()

        preferencesEditor!!.putBoolean(KEY_NEVER_REMIND_AGAIN, true).commit()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow))).check(doesNotExist())
    }

    @Test
    fun dialogShouldNotPopupWhenRequirementsAreNotMet() {
        setupAndClearSharedPrefs()

        preferencesEditor!!.putInt(KEY_LAUNCH_COUNT, 1).commit()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        preferencesEditor!!.putLong(KEY_FIRST_START_DATE, cal.timeInMillis).commit()

        activityTestRule.launchActivity(null)

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow))).check(doesNotExist())
    }

    @Test
    fun checkButtonVisibility() {
        setupAndClearSharedPrefs()

        activityTestRule.launchActivity(null)
        //All Buttons are visible
        val buttonDefault = onView(allOf<View>(withId(R.id.buttonDefaultDialog), isDisplayed()))
        buttonDefault.perform(click())

        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button2), withText(R.string.remindNever), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button3), withText(R.string.remindLater), isDisplayed())).check(matches(isDisplayed()))

        Espresso.pressBack()

        //No remind never button
        onView(allOf<View>(withId(R.id.buttonNoRemindNever), isDisplayed())).perform(click())
        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button3), withText(R.string.remindLater), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button2))).check(matches(not(isDisplayed())))

        Espresso.pressBack()

        //No remind later button
        onView(allOf<View>(withId(R.id.buttonNoRemindLater), isDisplayed())).perform(click())
        onView(allOf<View>(withId(android.R.id.button1), withText(R.string.rateNow), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button2), withText(R.string.remindNever), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(android.R.id.button3))).check(matches(not(isDisplayed())))
    }
}