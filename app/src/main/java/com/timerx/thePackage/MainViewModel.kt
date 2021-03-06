package com.timerx.thePackage

import analysis.*
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import generators.DataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

/**
 * Holds all the data of the app to be retrieved by MainActivity. As a [ViewModel],
 * it outlives the activity, so the data is not re-retrieved between configuration
 * changes.
 * @author Brian Smith
 * @see [MainActivity]
 */
class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    var dataProcessor: DataProcessor
    var currentRecord: ExerciseRecord?
    var colors: HashMap<String, Int>
    var settings: Settings

    init{
        Log.i("MainViewModel", "ViewModel Constructed")

        dataProcessor = DataProcessor()
        colors = HashMap()
        currentRecord = null
        settings = Settings(mutableListOf(), mutableListOf(), null)


        viewModelScope.launch(Dispatchers.Default) {
            settings = FileManager.loadSettings(getApplication())
            loadColors()
            currentRecord = settings.currentRecord
            Log.i("MainViewModel", "Current record: $currentRecord")
            readRecords(colors.keys.toList())    // calls sortWeeks() if needed
            dataProcessor.runAnalysis(
                getAnalysisProvider(),
                getMetaAnalyses()
            ) // this line should be kept
            dataProcessor.findRelevantResults()
            Log.i("MainViewModel", "Relevant Results found")
        }
    }



    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "View Model cleared")
    }



    private fun getAnalysisProvider() : (String, Int) -> MutableList<Analysis> {
        return { _: String, w: Int ->
            mutableListOf(
                LongestSessionAnalysis(w),
                TotalLengthAnalysis(w)
            )
        };
    }

    private fun getMetaAnalyses(): Array<MetaAnalysis> {
        return arrayOf(
            LengthEachDayMetaAnalysis(null),
            TimeOfDayMetaAnalysis(null),
            TotalLengthMetaAnalysis(null)
        )
    }



    val numWeeks = 5

    /**
     * Starts coroutines to read all the exercise records from this week and
     * to read all the analyses from the last 5 weeks except this one.
     * No meta analyses are read.
     * (Meta analyses are not saved in internal storage.) Meta analyses should
     * be obtained by calling
     * `runAnalyses((name, w) -> { return new Analysis[0]; }, metaAnalyses)`
     * @param context the context to which the records belong.
     */
    suspend fun readRecords(
        names: List<String>
    ) {
        dataProcessor.sortWeeks()
        for (w in 0 until numWeeks) {
            // Load ExerciseRecords
            FileManager.loadWeek(getApplication(), w)
            if (FileManager.weekLoaded(w))
                for (er in FileManager.weeks[w]!!.records)
                    dataProcessor.addRecord(er.title, er)

            if(w != 0) {
                // Load AnalysisResults
                FileManager.loadAnalysis(getApplication(), DataProcessor.analysisKeys, names, w)
                val results: List<AnalysisResult> =
                    FileManager.analysisList[w] ?: listOf()
                if (dataProcessor.relevantResults == null)
                    dataProcessor.initRelevantResults()
                dataProcessor.relevantResults.addAll(results)
                if (w == numWeeks - 1)
                    dataProcessor.isRelevantResultsReady = true
            }
        }
    }

    suspend fun writeRecords() = writeRecords(0)

    suspend fun writeRecords(w: Int) {
        dataProcessor.sortWeeks()
        val weekRecords = ArrayList<ExerciseRecord>()
        if (dataProcessor.weeks != null) {
            dataProcessor.weeks.forEach { (name: String?, ws: ArrayList<ExerciseWeek?>) ->
                if (ws[w] != null) weekRecords.addAll(ws[w]!!.records)
            }
            Log.i("MainViewModel", "Saving week $w")
            FileManager.saveWeek(getApplication(), w, weekRecords)
            Log.i("MainViewModel", "Week $w saved")
        }
        if (dataProcessor.organizedResults != null) {
            val results = ArrayList<AnalysisResult>()
            dataProcessor.organizedResults.forEach { (name: String?, exercise: Array<Array<AnalysisResult?>?>) ->
                if (name == DataProcessor.META_ANALYSIS_TITLE) return@forEach
                if (w >= exercise.size) return@forEach
                val week = exercise[w]
                if (week != null)
                    results.addAll(listOf(*week))
            }
            FileManager.saveAnalyses(getApplication(), 0, results)
        }
    }

    suspend fun writeSettings() = FileManager.saveSettings(getApplication(), settings)

    private fun loadColors(){
        for(i in settings.exerciseNames.indices)
            colors[settings.exerciseNames[i]] =
                settings.exerciseColors[i]
    }

    fun saveColorsToSettingsObject() {
        settings.exerciseNames = mutableListOf()
        settings.exerciseColors = mutableListOf()
        for((name, color) in colors){
            settings.exerciseNames.add(name)
            settings.exerciseColors.add(color)
        }
    }

    fun saveCurrentRecordToSettingsObject() {
        currentRecord?.endTime = Calendar.getInstance().timeInMillis
        settings.currentRecord = currentRecord
    }

    fun storeCurrentRecordToDataProcessor(){
        if(currentRecord != null){
            if(currentRecord!!.endTime == -1L)
                currentRecord!!.endTime = Calendar.getInstance().timeInMillis
            Log.i("MainViewModel", "ExerciseCreated ${DataProcessor.formatTime(currentRecord!!.length())}")
            dataProcessor.addRecord(currentRecord!!.title, currentRecord)
            dataProcessor.sortWeeks()
            currentRecord = null
        }
    }

    fun createCurrentRecord(name: String){
        storeCurrentRecordToDataProcessor()
        currentRecord = ExerciseRecord(
            name,
            Calendar.getInstance().timeInMillis,
            -1
        )
    }

    fun clearAllData(){
        FileManager.deleteAllFiles(getApplication())
        dataProcessor = DataProcessor()
        dataProcessor.runAnalysis(getAnalysisProvider(), getMetaAnalyses())
        dataProcessor.findRelevantResults()
        currentRecord = null
        colors = HashMap()
        settings = Settings(mutableListOf(), mutableListOf(), null)
    }

    fun generateData() {
        dataProcessor = DataProcessor()
        colors = DataGenerator.generateData(dataProcessor, DataGenerator.DAY_TIME)
        dataProcessor.runAnalysis(getAnalysisProvider(), getMetaAnalyses())
        dataProcessor.findRelevantResults()
        currentRecord = null
        settings = Settings(colors.keys.toMutableList(), colors.values.toMutableList(), null)
        viewModelScope.launch{ writeRecords(1) }
    }

}