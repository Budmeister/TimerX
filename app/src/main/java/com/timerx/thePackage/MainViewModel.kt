package com.timerx.thePackage

import analysis.*
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

//    private val liveSettings: MutableLiveData<Settings>
    var dataProcessor: DataProcessor
    var currentRecord: ExerciseRecord?
    var colors: HashMap<String, Int>
    var settings: Settings

    init{
        // TODO create MainViewModel
        Log.i("MainViewModel", "ViewModel Constructed")

        dataProcessor = DataProcessor()
        colors = HashMap()
        currentRecord = null
        settings = Settings(mutableListOf(), mutableListOf(), null)


        viewModelScope.launch(Dispatchers.Default) {
//            liveSettings.postValue(loadSettings(getApplication()))
            settings = FileManager.loadSettings(getApplication())
            Log.d("MainViewModel", "$settings")
            loadColors()
            currentRecord = settings.currentRecord
            Log.d("MainViewModel", "$currentRecord")
            // testing
//            dataProcessor = DataGenerator.generateData(DataGenerator.GAUSSIAN, 0)
//            DataGenerator.generateData(dataProcessor, DataGenerator.GAUSSIAN, 1)
            // real
//            dataProcessor.readRecords(this@MainActivity, colors.keys.toList())    // replaced with MainActivity.readRecords()
            readRecords(colors.keys.toList())    // calls sortWeeks() if needed
            dataProcessor.runAnalysis(
                getAnalysisProvider(),
                getMetaAnalyses()
            ) // this line should be kept
//            dataProcessor.sortWeeks()   // delete this line
            dataProcessor.findRelevantResults()
            Log.i("MainViewModel", "Relevant Results found")
        }
    }



    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "View Model cleared")
//        if(currentRecord != null){
//            currentRecord!!.endTime = Calendar.getInstance().timeInMillis
//            Log.d("MainViewModel", "Exercise created ${DataProcessor.formatTime(currentRecord!!.length())}")
//            dataProcessor.addRecord(currentRecord!!.title, currentRecord)
//            currentRecord = null
//        }
//        Log.d("MainViewModel", "${Thread.currentThread()}")
//        viewModelScope.launch(Dispatchers.Default) {
//            // save files
//            Log.d("MainViewModel", "cleared coroutine started")
//            writeRecords()
//            saveColorsToSettingsObject()
//            FileManager.saveSettings(getApplication(), settings)
//        }
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
        FileManager.loadWeek(getApplication(), 0)
        if (FileManager.weekLoaded(0))
            for (er in FileManager.weeks[0]!!.records)
                dataProcessor.addRecord(er.title, er)
        dataProcessor.sortWeeks()
        val numWeeks = 5
        for (w in 1 until numWeeks) {
            FileManager.loadAnalysis(getApplication(), DataProcessor.analysisKeys, names, w)
            val results: List<AnalysisResult> =
                FileManager.analysisList[w]?: listOf()
            if(dataProcessor.relevantResults == null)
                dataProcessor.initRelevantResults()
            dataProcessor.relevantResults.addAll(results)
            Log.d("DataProcessor", "" + w)
            if (w == numWeeks - 1)
                dataProcessor.isRelevantResultsReady = true
        }

        // do not move to next state, because multiple files can be read
        // before sorting begins
    }

    suspend fun writeRecords() {
        dataProcessor.sortWeeks()
        val weekRecords = ArrayList<ExerciseRecord>()
        Log.d("MainViewModel", "${dataProcessor.weeks}")
        if (dataProcessor.weeks != null) {
            dataProcessor.weeks.forEach { (name: String?, ws: ArrayList<ExerciseWeek?>) ->
                if (ws[0] != null) weekRecords.addAll(ws[0]!!.records)
            }
            Log.d("MainViewModel", "Saving week")
            FileManager.saveWeek(getApplication(), 0, weekRecords)
            Log.d("MainViewModel", "Week saved")
        }
        if (dataProcessor.organizedResults != null) {
            val results = ArrayList<AnalysisResult>()
            dataProcessor.organizedResults.forEach { (name: String?, exercise: Array<Array<AnalysisResult?>?>) ->
                if (name == DataProcessor.META_ANALYSIS_TITLE) return@forEach
                val week = exercise[0]
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
//        for(i in liveSettings.value!!.exerciseNames.indices)
//            liveColors.value!![liveSettings.value!!.exerciseNames[i]] =
//                liveSettings.value!!.exerciseColors[i]
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
            Log.d("MainViewModel", "ExerciseCreated ${DataProcessor.formatTime(currentRecord!!.length())}")
            dataProcessor.addRecord(currentRecord!!.title, currentRecord)
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

}