package com.timerx.thePackage

import analysis.AnalysisResult
import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

//@Serializer(MyModel(1,"2"))
class FileManager {
    companion object {

        var weeks = mutableListOf<ExerciseWeek?>()
        var analysisList = ArrayList<MutableList<AnalysisResult>?>()

        private fun recordFileName(w: Int) : String =
            "${MainActivity.viewModel.dataProcessor.getWeekStart(w)}.json"

        private fun analysisFileName(type: String, w: Int, title: String) : String =
            "${type}_${MainActivity.viewModel.dataProcessor.getWeekStart(w)}_${title}.json"

        const val settingsDir = "settings.json"

        fun weekLoaded(w: Int): Boolean =
            w >= 0 && w < weeks.size && weeks[w] != null

        suspend fun loadWeek(context: Context, w: Int) : Boolean {
            var week = listOf<ExerciseRecord>()
            val fileName = recordFileName(w)
            var failure = false
            withContext(Dispatchers.IO){
                try {
                    context.openFileInput(fileName).use{ stream ->
                        val jsonData = stream.readBytes().decodeToString()
                        week = Json.decodeFromString(ListSerializer(ExerciseRecord.serializer()), jsonData)
                        Log.i("FileManager", "Loaded: $fileName")
                        Log.i("FileManager", "Decoded: $jsonData")
                    }
                } catch (e: Exception){
                    when(e) {
                        is IOException, is SerializationException ->
                            failure = true
                        else ->
                            throw e
                    }
                }
            }
            if(failure)
                return false
            while(weeks.size <= w)
                weeks.add(null)
            val curWeek = weeks[w]
            if(curWeek == null) {
                val alistweek = DataProcessor.toArrayList(week)
                val eweek = ExerciseWeek(alistweek)
                weeks[w] = eweek
            }else{
                curWeek.loadRecords(week)
            }
            return true
        }

        suspend fun loadAnalysis(context: Context, types: Array<String>, names: List<String>, w: Int) : Array<Boolean>{
            val results = mutableListOf<AnalysisResult>()
            val successes = Array(types.size * names.size){ false }
            var count = 0
            return withContext(Dispatchers.IO){
                for(i in types.indices){
                    val type = types[i]
                    for(name in names) {
                        val fileName = analysisFileName(type, w, name)
                        try {
                            context.openFileInput(fileName).use { stream ->
                                val jsonData = stream.readBytes().decodeToString()
                                val result = Json.decodeFromString(
                                    AnalysisResult.serializer(),
                                    jsonData
                                )
                                results.add(result)
                                Log.i("FileManager", "Loaded: $fileName")
                                Log.i("FileManager", "Decoded: $jsonData")
                            }
                            successes[count++] = true
                        } catch (e: Exception) {
                            when(e) {
                                is IOException, is SerializationException ->
                                    successes[count++] = false
                                else ->
                                    throw e
                            }
                        }
                    }
                }
                Log.i("FileManager", "Analyses loaded; types: ${Arrays.toString(types)}, Results: $results")
                // this line is being run
                // but this line is not
                while(analysisList.size <= w)
                    analysisList.add(null)
                val curList = analysisList[w]
                if(curList == null) {
                    val aresults = DataProcessor.toArrayList(results)
                    analysisList[w] = aresults
                }else{
                    curList.addAll(results)
                }
                successes
            }
        }

        suspend fun loadSettings(context: Context): Settings = withContext(Dispatchers.IO) {
            var settings: Settings
            try {
                    context.openFileInput(settingsDir).use { stream ->
                        val jsonData = stream.readBytes().decodeToString()
                        settings = Json.decodeFromString(Settings.serializer(), jsonData)
                    }
            } catch (e: Exception){
                when(e) {
                    is IOException, is SerializationException ->
                        settings = Settings(mutableListOf(), mutableListOf(), null)
                    else ->
                        throw e
                }
            }
            settings
        }

        suspend fun saveWeek(context : Context, w: Int, records : List<ExerciseRecord>) = withContext(Dispatchers.IO){
            val fileName = recordFileName(w)
            try{
                context.openFileOutput(fileName, Context.MODE_PRIVATE).use{ stream ->
                    val jsonData = Json.encodeToString(records)
                    Log.i("FileManager", "Saved: $fileName")
                    Log.i("FileManager", "Encoded: $jsonData")
                    stream.write(jsonData.encodeToByteArray())
                }
                true
            } catch (e: IOException){
                e.printStackTrace()
                false
            }
        }

        suspend fun saveAnalyses(context: Context, w: Int, results: List<AnalysisResult>){
            results.forEach{ result -> saveAnalysis(context, w, result) }
         }

        suspend fun saveAnalysis(context: Context, w: Int, result: AnalysisResult) : Boolean{
            val fileName = analysisFileName(result.key(), w, result.title?: "untitled")
            return try{
                context.openFileOutput(fileName, Context.MODE_PRIVATE).use{ stream ->
                    val jsonData = Json.encodeToString(result)
                    stream.write(jsonData.encodeToByteArray())
                    Log.i("FileManager", "Saved: $fileName")
                    Log.i("FileManager", "Encoded: $jsonData")
                }
                true
            } catch (e: IOException){
                e.printStackTrace()
                false
            }
        }

        suspend fun saveSettings(context: Context, settings: Settings){
            try{
                context.openFileOutput(settingsDir, Context.MODE_PRIVATE).use { stream ->
                    val jsonData = Json.encodeToString(settings)
                    stream.write(jsonData.encodeToByteArray())
                    Log.i("FileManager", "Saved settings: $jsonData")
                }
            } catch (e: IOException){
                e.printStackTrace()
            }
        }

    }

}