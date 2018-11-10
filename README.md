# RunnersClock

10-11-2018 GitHub README

Purpose: Runner's Clock is created as an android exercise as well as for personal use while jogging. It shows a chronometer which if started will  vibrate in a pattern that indicates the number of minutes past. 
The number of minutes < 10 is translated into a series of short vibrations. Minutes >= 10 result in a series of vibrations with a duration twice as long, hours again twice as long.
To enhance recognition minutes are grouped by 5 and the pattern stays in a rhytm. 

Unresolved:

I did not succeed yet to find a graceful way to ensure that vibration continues in doze mode. 
As from Android M Battery Optimization is introduced which is very complicated to circumvent. Also the App Store seems  not to accept apps with android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

Also it is not sufficient to just prompt the user to allow Ignore Battery Optimization. 
After this the user also has to disable Launch > Automatically (In Huawei Oreo 8.0 Apps > All Apps > [app name] > Battery > Launch > Set off: Manage automatically
Or by: Settings > Battery > Launch > [app name ] > Set off: Manage automatically

The best I could come up so far is to show several dialogs to prompt the user to make the necessary setting changes.
There seems to be no unified way to do this programatically. Even to get the user to the right place to do this himself is difficult. See 
https://stackoverflow.com/questions/48166206/how-to-start-power-manager-of-all-android-manufactures-to-enable-background-and
