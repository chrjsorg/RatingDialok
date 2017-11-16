package org.chrjs.ratingdialok

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.util.Log
import java.lang.ref.WeakReference
import java.util.Date
import kotlin.collections.ArrayList

class RatingDialok(ctx: Context) {

    companion object {
        private val SHARED_PREF_NAME = "ratingDialok"
        private val KEY_USER_HAS_RATED = "RD_KEY_USER_HAS_RATED"
        private val KEY_NEVER_REMIND_AGAIN = "RD_KEY_NEVER_REMIND_AGAIN"
        private val KEY_FIRST_START_DATE = "RD_KEY_FIRST_START_DATE"
        private val KEY_LAUNCH_COUNT = "RD_KEY_LAUNCH_COUNT"
    }

    /**
     * Custom Additional Condition
     */
    interface Condition {
        fun conditionMet(): Boolean
    }

    /**
     * Action Callback for the Dialog Actions
     */
    interface ActionCallback {
        fun remindLaterClicked()
        fun rateNowClicked()
        fun remindNeverAgainClicked()
    }

    private val context: WeakReference<Context> = WeakReference(ctx)
    private val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, 0)

    /**
     * Set ActionCallback to receive actions clicked on the dialog
     */
    @Suppress("MemberVisibilityCanPrivate")
    var actionCallback: ActionCallback? = null

    private var additionalConditions: ArrayList<Condition> = ArrayList()

    /**
     * Set to true if you want to have an "or" condition instead of an "and" condition
     */
    @Suppress("MemberVisibilityCanPrivate")
    var useOrConditionForDaysAfterAndLaunchCount = false

    /**
     * Indicates how many times the activity should start before showing the dialog
     */
    @Suppress("MemberVisibilityCanPrivate")
    var minimumLaunchCount = 5

    /**
     * Indicates how many days after the first start the activity should start before showing the dialog
     */
    @Suppress("MemberVisibilityCanPrivate")
    var minimumDaysAfter = 7

    /**
     * Indicates if the Dialog should be cancelable or not
     */
    @Suppress("unused")
    var isCancelable: Boolean = true

    /**
     * Theme for the Dialog, if none is supplied the context theme will be used
     */
    @Suppress("MemberVisibilityCanPrivate")
    var resourceIdStyle: Int = 0

    private var resourceIdTitle: Int? = null
    private var resourceIdMessage: Int? = null
    private var resourceIdRateNow: Int? = null
    private var resourceIdRemindLater: Int? = null
    private var resourceIdRemindNever: Int? = null

    private var dialog: Dialog? = null

    /**
     * Returns true if the dialog is currently shown
     */
    @Suppress("MemberVisibilityCanPrivate")
    val isShowing: Boolean
        get() = dialog?.isShowing ?: false

    /**
     * Adds an (additional) [Condition]
     */
    @Suppress("unused")
    fun addAdditionalCondition(condition: Condition) = additionalConditions.add(condition)

    /**
     * Removes an (additional) [Condition]
     */
    @Suppress("unused")
    fun removeAdditionalCondition(condition: Condition) = additionalConditions.remove(condition)

    /**
     * If you supply null for a string id the button/field won't be shown.
     */
    @Suppress("unused")
    fun setStrings(rIdTitle: Int?, rIdMessage: Int, rIdRateNowButtonText: Int,
                   rIdRemindLaterButtonText: Int?, rIdRemindNeverAgainButtonText: Int?) {
        resourceIdTitle = rIdTitle
        resourceIdMessage = rIdMessage
        resourceIdRateNow = rIdRateNowButtonText
        resourceIdRemindLater = rIdRemindLaterButtonText
        resourceIdRemindNever = rIdRemindNeverAgainButtonText
    }

    /**
     * Call in Activity.onStart or Fragment.onCreateView or wherever you want.
     * Increases the launchTimes and sets the first day the app is used if applicable
     */
    @Suppress("unused")
    fun onStart() {
        if (userDidRate() || userSetNeverRemindAgain()) return

        var launchCount = sharedPreferences.getInt(KEY_LAUNCH_COUNT, 0)
        val firstLaunchDate = sharedPreferences.getLong(KEY_FIRST_START_DATE, -1L)

        //Register first launch if we dont have it yet
        if (firstLaunchDate == -1L) registerFirstLaunchDate()

        setLaunchCount(++launchCount)
    }

    /**
     * Show the dialog no matter the conditions.
     * Should be used for debug only
     */
    @Suppress("unused")
    fun showDialogNoMatterWhat() {
        showDialog()
    }

    private fun additionalConditionsAreMet(): Boolean = additionalConditions.all { it.conditionMet() }

    /**
     * Checks [additionalConditions], minimumDaysAfter and minimumLaunches if all conditions are met,
     * the dialog will be shown.
     */
    @Suppress("unused")
    fun showDialogIfNeeded() {
        if (areConditionsMet()) showDialog()
    }

    private fun setNeverRemindAgain() {
        sharedPreferences.edit().putBoolean(KEY_NEVER_REMIND_AGAIN, true).apply()
        actionCallback?.remindNeverAgainClicked()
    }

    /**
     * Starts a PlayStore Intent for the app id or if the PlayStore is not installed a browser intent to show the app
     */
    @Suppress("MemberVisibilityCanPrivate")
    fun rateNow() {
        val appPackageName = context.get()?.packageName
        try {
            context.get()?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)))
        } catch (e: ActivityNotFoundException) {
            context.get()?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
        }

        sharedPreferences.edit().putBoolean(KEY_USER_HAS_RATED, true).apply()
        actionCallback?.rateNowClicked()
    }

    /**
     * Remind Me Later Clicked
     */
    private fun setRemindLater() {
        actionCallback?.remindLaterClicked()
        setLaunchCount(0)
        registerFirstLaunchDate()
    }

    /**
     * Returns true if the user rated the app already
     */
    @Suppress("MemberVisibilityCanPrivate")
    fun userDidRate(): Boolean =
            sharedPreferences.getBoolean(KEY_USER_HAS_RATED, false)

    /**
     * Returns true if the user selected "Never remind me again"
     */
    @Suppress("MemberVisibilityCanPrivate")
    fun userSetNeverRemindAgain(): Boolean =
            sharedPreferences.getBoolean(KEY_NEVER_REMIND_AGAIN, false)

    private fun areConditionsMet(): Boolean {
        if ((userDidRate()) || userSetNeverRemindAgain()) return false
        if (!additionalConditionsAreMet()) return false

        val launchCount = sharedPreferences.getInt(KEY_LAUNCH_COUNT, 0)
        val firstLaunchDate = sharedPreferences.getLong(KEY_FIRST_START_DATE, 0L)

        return if (useOrConditionForDaysAfterAndLaunchCount)
            daysBetween(firstLaunchDate, Date().time) > minimumDaysAfter || launchCount > minimumLaunchCount
        else
            daysBetween(firstLaunchDate, Date().time) > minimumDaysAfter && launchCount > minimumLaunchCount
    }

    private fun showDialog() {
        if (isShowing)
            return

        try {
            dialog = null
            dialog = createDialog()
            dialog?.show()
        } catch (e: Exception) {
            //Catch all exceptions to prevent a crash due to conflicts with the UI thread.
            // For example: "Expired Window Token", IllegalStateException, "BadTokenException", ...
            Log.e(javaClass.simpleName, e.message)
        }
    }

    private fun setLaunchCount(launchCount: Int) = sharedPreferences.edit().putInt(KEY_LAUNCH_COUNT, launchCount).apply()

    /**
     * Register current date for the first launch
     */
    private fun registerFirstLaunchDate() = sharedPreferences.edit().putLong(KEY_FIRST_START_DATE, Date().time).apply()

    /**
     * Calculates days between 2 dates
     */
    private fun daysBetween(firstDate: Long, lastDate: Long): Long =
            (lastDate - firstDate) / (1000 * 60 * 60 * 24)

    private fun createDialog(): Dialog? {
        if (context.get() == null) return null
        val builder = AlertDialog.Builder(context.get()!!, resourceIdStyle).apply {
            setMessage(resourceIdMessage!!)
            setPositiveButton(resourceIdRateNow!!, { _, _ -> rateNow() })
            setOnCancelListener({ setRemindLater() })

            //Optional field title
            resourceIdTitle?.let { setTitle(it) }

            //Optional Button remind later
            resourceIdRemindLater?.let { setNeutralButton(it, { _, _ -> setRemindLater() }) }

            //Optional Button remind never
            resourceIdRemindNever?.let { setNegativeButton(it, { _, _ -> setNeverRemindAgain() }) }
        }

        return builder.create()
    }
}

