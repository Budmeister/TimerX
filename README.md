# TimerX

Track your time with TimerX.

Effortlessly Collect Data.<br>
Access Immediate Insignts.<br>
Revolutionize your Schedule.<br>
Achieve Maximum Output.

## Tutorial
From the home screen, click the add button to create an "exercise". An 
exercise is just an activity you want to track.
Once you have the your exercises created, tap one to start the recording.
If you want to delete an exercise, hold it down and confirm by tapping "yes".

Statistics about all your exercises appear in the feed page.

You can see all your history on the calendar page. Swipe left to see last week.

Statistic specific to one exercise appear in the stats page on the right. Also
in the stats page, you can generate data to see it in action, or you can clear
your data to start fresh.


## How it works
Each time you record an exercise, an `ExerciseRecord` is created, and each time the
app starts up, a set of analyses are run on all the `ExerciseRecord`'s of this week.
Each analysis is an object of a specific subclass of `Analysis`. All of the specific
analyses are:<br>
* `LongestSessionAnalysis`
* `TotalLength`

Each `Analsysis` represents an anlysis of the records of one exercise in one week, 
and it produces one or more `AnalysisResult`'s. So there will be one of each type of 
`AnalysisResult` for each exercise for each week. Most `AnalysisResult`'s create a 
view in the Stats page. Then, the set of `AnalysisResult`'s are sent to the 
`MetaAnalysis` objects which analyize long-term trends in the data. Each `MetaAnalysis` 
accepts all the `AnalysisResult`'s of one type from all the exercises from all (only the 
last 5) weeks. And it produces a set of `MetaAnalysisResult`'s, each of which creates 
a view in the Feed page. So there will be one `MetaAnalysisResult` of each type for each
week. All of the types of `MetaAnalysis` are:
* `LengthEachDayMetaAnalysis`
* `TimeOfDayMetaAnalysis`
* `TotalLengthMetaAnalysis`

Individual `ExerciseRecord`'s from all weeks are displayed in the `CalendarView`.

## Thanks to
* https://github.com/Dhaval2404/ColorPicker
* https://github.com/Kotlin/kotlinx.serialization
