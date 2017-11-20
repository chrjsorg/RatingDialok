# RatingDialok 
[![Build Status](https://travis-ci.org/chrjsorg/RatingDialok.svg?branch=master)](https://travis-ci.org/chrjsorg/RatingDialok)
![Release](https://jitpack.io/v/chrjsorg/RatingDialok.svg)
(https://jitpack.io/#chrjsorg/RatingDialok.svg)

Customizable Android Rating Dialog written in Kotlin, provides a simple way to display a rating alert dialog.

<img src="https://raw.githubusercontent.com/chrjsorg/RatingDialok/master/screenshots/screenshot.png" width="310px">

## Usage
### Download
Add this to your root *build.gradle* file:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your app build.gradle file:

```
compile 'com.github.chrjsorg:RatingDialok:<VERSION>'
```
For the latest version see [releases](https://github.com/chrjsorg/RatingDialok/releases)

### Settings
```kotlin
private fun setupRatingDialog() {
    ratingDialog = RatingDialok(this).apply {
        //Is the dialog cancelable?
        isCancelable = true

        //Minimum Days after first launch?
        minimumDaysAfter = 7

        //Minimum launch counter
        minimumLaunchCount = 5
        
        //Only one of the two required conditions must be met
        useOrConditionForDaysAfterAndLaunchCount = true

        //Style/Theme
        resourceIdStyle = R.style.CustomAlertDialogStyle

        //Strings
        setStrings(R.string.title, R.string.message, R.string.rateNow, R.string.remindLater,
                R.string.remindNever)


        //Action Callback (optional)
        actionCallback = customActionCallback

        //Additional Condition besides the launchCounter / minimumDaysAfter (optional)
        addAdditionalCondition(conditionTrigger)
    }
}
```


### Flow
Create dialog in an activity or fragment, with the `onStart()` method you initialize the first start of the app and increase the launch counter.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setupRatingDialog()
    ratingDialog.onStart()
}
```

Open the dialog when needed:

```kotlin
override fun onResume() {
    super.onResume()
    ratingDialog.showDialogIfNeeded()
}
```

### ActionCallback
You can register for callbacks when the user taps something in the dialog

```kotlin
private var customActionCallback: RatingDialok.ActionCallback = object : RatingDialok.ActionCallback {
    override fun remindLaterClicked() {
        Toast.makeText(this@MainActivity, "Remind later clicked", Toast.LENGTH_SHORT).show()
    }

    override fun rateNowClicked() {
        Toast.makeText(this@MainActivity, "Rate Now Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun remindNeverAgainClicked() {
        Toast.makeText(this@MainActivity, "Remind Never Again clicked", Toast.LENGTH_SHORT).show()
    }
    
    override fun dialogShown() {
        Toast.makeText(this@MainActivity, "Dialog was shown", Toast.LENGTH_SHORT).show()
    }
}

ratingDialog.actionCallback = customActionCallback
```

### Additional Conditions
You can add additional conditions to the existing ones

```kotlin
private var condition: RatingDialok.Condition = object : RatingDialok.Condition {
    override fun conditionMet(): Boolean {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) % 2 == 0
    }
}

ratingDialog.addAdditionalCondition(condition);
```
If you want only your custom conditions to apply, you can set both default conditions to `-1` so they will apply right away

```kotlin
  ratingDialog.minimumDaysAfter = 0
  ratingDialog.minimumLaunchCount = 0
```
### Debug
You can use `.showNoMatterWhat()` instead of `.showDialogIfNeeded()` to display the dialog all the time and test it.

### Other
You can call `ratingDialog.rateNow()` to provide the functionality (Rate your app) when you want to.

If you want to check if the user already rated your app or dismissed the dialog forever you can use `.userDidRate()` or `.userSetNeverRemindAgain()`

If you want to reset the saved data after a huge update for example you can use `.reset()` so the flow begins from the scratch.

### Customization via styles.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
     <style name="CustomAlertDialogStyle" parent="Theme.AppCompat.Light.Dialog.Alert">
        <!-- Used for the title and text -->
        <item name="android:textColorPrimary">#000000</item>
        <!-- Used for the background -->
        <item name="android:background">#ffffff</item>
        <item name="buttonBarNegativeButtonStyle">@style/NegativeButtonStyle</item>
        <item name="buttonBarPositiveButtonStyle">@style/PositiveButtonStyle</item>
        <item name="buttonBarNeutralButtonStyle">@style/NeutralButtonStyle</item>
    </style>

    <!--Style for the negative Button-->
    <style name="NegativeButtonStyle" parent="Widget.AppCompat.Button.ButtonBar.AlertDialog">
        <item name="android:textColor">#fd7171</item>
    </style>

    <!--Style for the positive button-->
    <style name="PositiveButtonStyle" parent="Widget.AppCompat.Button.ButtonBar.AlertDialog">
        <item name="android:textColor">#55894b</item>
    </style>

    <!--Style for the neutral button-->
    <style name="NeutralButtonStyle" parent="Widget.AppCompat.Button.ButtonBar.AlertDialog">
        <item name="android:textColor">#979b9a</item>
    </style>
</resources>
```

Set the theme on the dialog: `ratingDialog.resourceIdStyle = R.style.CustomAlertDialogStyle`

### Espresso Tests
`./gradlew connectedAndroidTest`

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
