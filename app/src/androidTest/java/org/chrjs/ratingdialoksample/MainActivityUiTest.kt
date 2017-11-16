package org.chrjs.ratingdialoksample

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
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


@Suppress("RedundantVisibilityModifier")
@RunWith(AndroidJUnit4::class)
public class MainActivityUiTest {

    @Rule
    @JvmField
    public var mActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun checkButtonVisibility() {
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
    }
}