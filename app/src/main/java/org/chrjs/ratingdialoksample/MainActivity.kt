package org.chrjs.ratingdialoksample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.chrjs.ratingdialok.RatingDialok
import java.util.*

@Suppress("RedundantVisibilityModifier")
class MainActivity : AppCompatActivity() {

    private lateinit var ratingDialog: RatingDialok

    private var rIdRemindNever: Int? = R.string.remindNever
    private var rIdRemindLater: Int? = R.string.remindLater

    public var remindLaterClicked: Boolean = false
    public var remindNeverClicked: Boolean = false
    public var rateNowClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRatingDialog()
        buttonDefaultDialog.setOnClickListener {
            rIdRemindNever = R.string.remindNever
            rIdRemindLater = R.string.remindLater
            setupRatingDialog()
            ratingDialog.showDialogNoMatterWhat()
        }
        buttonNoRemindNever.setOnClickListener {
            rIdRemindNever = null
            rIdRemindLater = R.string.remindLater
            setupRatingDialog()
            ratingDialog.showDialogNoMatterWhat()
        }
        buttonNoRemindLater.setOnClickListener {
            rIdRemindNever = R.string.remindNever
            rIdRemindLater = null
            setupRatingDialog()
            ratingDialog.showDialogNoMatterWhat()
        }
        buttonReset.setOnClickListener {
            ratingDialog.reset()
        }
        buttonRateNow.setOnClickListener { ratingDialog.rateNow() }
        ratingDialog.onStart()
    }

    override fun onResume() {
        super.onResume()
        ratingDialog.showDialogIfNeeded()
    }

    private fun setupRatingDialog() {
        ratingDialog = RatingDialok(this).apply {
            isCancelable = false
            minimumDaysAfter = 7
            minimumLaunchCount = 5
            useOrConditionForDaysAfterAndLaunchCount = true
            resourceIdStyle = R.style.CustomAlertDialogStyle
            setStrings(R.string.title, R.string.message, R.string.rateNow, rIdRemindLater,
                    rIdRemindNever)
            actionCallback = customActionCallback
            //addAdditionalCondition(condition)
        }
    }

    private var condition: RatingDialok.Condition = object : RatingDialok.Condition {
        override fun conditionMet(): Boolean {
            val cal = Calendar.getInstance()
            return cal.get(Calendar.HOUR_OF_DAY) % 2 == 0
        }
    }

    private var customActionCallback: RatingDialok.ActionCallback = object : RatingDialok.ActionCallback {
        override fun remindLaterClicked() {
            remindLaterClicked = true
            Toast.makeText(this@MainActivity, "Remind later clicked", Toast.LENGTH_SHORT).show()
        }

        override fun rateNowClicked() {
            rateNowClicked = true
            Toast.makeText(this@MainActivity, "Rate Now Clicked", Toast.LENGTH_SHORT).show()
        }

        override fun remindNeverAgainClicked() {
            remindNeverClicked = true
            Toast.makeText(this@MainActivity, "Remind Never Again clicked", Toast.LENGTH_SHORT).show()
        }

        override fun dialogShown() {
            Toast.makeText(this@MainActivity, "Dialog was shown", Toast.LENGTH_SHORT).show()
        }
    }
}
