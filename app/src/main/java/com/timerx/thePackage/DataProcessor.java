package com.timerx.thePackage;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.BiFunction;

import analysis.Analysis;
import analysis.AnalysisResult;
import analysis.LengthEachDayAnalysisResult;
import analysis.LengthEachDayMetaAnalysisResult;
import analysis.LongestSessionAnalysisResult;
import analysis.MetaAnalysis;
import analysis.TimeOfDayAnalysisResult;
import analysis.TimeOfDayMetaAnalysisResult;
import analysis.TotalLengthAnalysisResult;
import analysis.TotalLengthMetaAnalysisResult;

/**
 * The class in charge of all the processing of data. Manages the {@code ExerciseWeek}s,
 * the {@code Analysis}'s, and the {@code FileManager}. 
 *
 */
public class DataProcessor {
	
	public static final String META_ANALYSIS_TITLE = null;
	
	public final long now;	// for sorting weeks
	public final Calendar cal;
	private final Calendar curWeekStart;
	private HashMap<String, ArrayList<ExerciseRecord>> data;
	/** Index 0 = this week, 1 = last week and so on */
	HashMap<String, ArrayList<ExerciseWeek>> weeks;
	
	// Exercise name -> AnalysisResult[][]: row=week with multiple analyses
	private HashMap<String, AnalysisResult[][]> organizedResults;
	private PriorityQueue<AnalysisResult> relevantResults;
	private boolean relevantResultsReady = false;

	public void setRelevantResultsReady(boolean rrr){
		relevantResultsReady = rrr;
	}

	public boolean isRelevantResultsReady(){
		return relevantResultsReady;
	}

	public static String[] analysisKeys = {
			LengthEachDayAnalysisResult.KEY,
			LongestSessionAnalysisResult.KEY,
			TimeOfDayAnalysisResult.KEY,
			TotalLengthAnalysisResult.KEY
	};

	public static String[] metaAnalysisKeys = {
			LengthEachDayMetaAnalysisResult.KEY,
			TimeOfDayMetaAnalysisResult.KEY,
			TotalLengthMetaAnalysisResult.KEY
	};
	
	public DataProcessor(){
		data = new HashMap<>();
		now = System.currentTimeMillis();
		cal = Calendar.getInstance();
		cal.setTime(new java.util.Date(now));
		
		curWeekStart = Calendar.getInstance();
		curWeekStart.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		curWeekStart.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR));
		curWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		for(int field : new int[] {
				Calendar.HOUR_OF_DAY,
				Calendar.MINUTE,
				Calendar.SECOND,
				Calendar.MILLISECOND
		})
			curWeekStart.set(field, 0);
	}
	
	/**
	 * Sorts the {@code ExerciseRecord}s by week into {@code weeks}.
	 * This should be called after all the data has been loaded.
	 * This method loads the exercise records from {@code data} to
	 * the {@code ExerciseWeek}s in {@code weeks} and resets {@code data}.
	 * It should be called before saving the data to internal storage or else
	 * some data may be missed.
	 */
	public void sortWeeks() {
		if(weeks == null)
			weeks = new HashMap<>();
		for (Map.Entry<String, ArrayList<ExerciseRecord>> entry : data.entrySet()) {
			String name = entry.getKey();
			ArrayList<ExerciseRecord> list = entry.getValue();
			// The ExerciseWeeks for this type of Exercise
			ArrayList<ExerciseWeek> weeksOf = weeks.computeIfAbsent(name, k -> new ArrayList<>());
			list.trimToSize();
			for (ExerciseRecord er : list) {    // loop through the data of this type of exercise
				int weekInd = getWeekIndex(er);    // get the number of the week this exercise happened in
				while (weekInd >= weeksOf.size())    // in case this is the first record in
					weeksOf.add(null);            // that week, expand our list of weeks
				ExerciseWeek week = weeksOf.get(weekInd);    // get the actual ExerciseWeek this exercise belongs to
				if (week == null)
					week = new ExerciseWeek();    // instantiate if it wasn't already
				week.addRecord(er);    // place this record in its proper week
				weeksOf.set(weekInd, week);    // replace the week in
			}
		}
		data = new HashMap<>();
	}
	
	public static final long MILLISECONDS_PER_SECOND = 
			1000;
	public static final long MILLISECONDS_PER_MINUTE = 
			MILLISECONDS_PER_SECOND * 60;
	public static final long MILLISECONDS_PER_HOUR = 
			MILLISECONDS_PER_MINUTE * 60;
	public static final long MILLISECONDS_PER_DAY = 
			MILLISECONDS_PER_HOUR * 24;
	public static final long MILLISECONDS_PER_WEEK = 
			MILLISECONDS_PER_DAY * 7;
	
	public int getWeekIndex(ExerciseRecord er) {
		return (int) ((curWeekStart.getTimeInMillis() + MILLISECONDS_PER_WEEK - er.getStartTime()) / MILLISECONDS_PER_WEEK);
	}
	
	public long getWeekStart(int weekInd) {
		return curWeekStart.getTimeInMillis() - weekInd * MILLISECONDS_PER_WEEK;
	}
	
	public long weekStart() {
		return curWeekStart.getTimeInMillis();
	}
	

	/**
	 * Runs analyses on all the weeks and all the exercises according to 
	 * the {@code Analysis}'s provided by {@code analysisProvider}. 
	 * This method stores its return value in {@code organizedResults},
	 * which will store the return value of this method's most recent call.
	 * 
	 * @param analysisProvider provides a {@code Collection} of {@code Analysis} objects 
	 * determined by the exercise title and week number. 
	 * @return A {@code HashMap} with exercise titles as the key and 
	 * arrays containing {@code AnalysisResult}s as the definition.
	 * These arrays contain the results of the given analyses with each
	 * row corresponding to a week and the items in the row corresponding
	 * to the {@code Collection}s of {@code Analysis}'s provided by {@code analysisProvider}.
	 */
	public HashMap<String, AnalysisResult[][]> runAnalysis(BiFunction<String, Integer, Collection<? extends Analysis>> analysisProvider) {
		return runAnalysis(analysisProvider, null);
	}
	
	/**
	 * Runs analyses on all the weeks and all the exercises according to 
	 * the {@code Analysis}'s provided by {@code analysisProvider}. 
	 * Then it runs the meta-analyses on their preferred exercises and
	 * weeks if they exist.
	 * This method stores its return value in {@code organizedResults},
	 * which will store the return value of this method's most recent call.
	 * 
	 * @param analysisProvider provides a {@code Collection} of {@code Analysis} objects 
	 * determined by the exercise title and week number. 
	 * @param metaAnalyses the list of meta-analyses, can be null
	 * @return A {@code HashMap} with exercise titles as the key and 
	 * arrays containing {@code AnalysisResult}s as the definition.
	 * These arrays contain the results of the given analyses with each
	 * row corresponding to a week and the items in the row corresponding
	 * to the {@code Collection}s of {@code Analysis}'s provided by {@code analysisProvider}.
	 */
	public HashMap<String, AnalysisResult[][]> runAnalysis(
			BiFunction<String, Integer, Collection<? extends Analysis>> analysisProvider,
			MetaAnalysis[] metaAnalyses) {
		sortWeeks();
		Log.d("DataProcessor", "Weeks: " + weeks + ", data: " + data);
		organizedResults = new HashMap<>();
		for (Map.Entry<String, ArrayList<ExerciseWeek>> entry : weeks.entrySet()) {
			String title = entry.getKey();
			ArrayList<ExerciseWeek> list = entry.getValue();
			AnalysisResult[][] results = new AnalysisResult[list.size()][];
			for (int i = 0; i < list.size(); i++) {
				ExerciseWeek week = list.get(i);
				if (week != null) {
					week.loadAnalyses(analysisProvider.apply(title, i));
					results[i] = week.calculateAnalyses();
				}
			}
			organizedResults.put(title, results);
		}
		if(metaAnalyses != null) {
			ArrayList<AnalysisResult> metaAnalysisResults = new ArrayList<>();
			
			for(MetaAnalysis ma : metaAnalyses) {
				String[] prefEx = ma.preferredExercises();
				int[] prefWeeks = ma.preferredWeeks();
				
				
				if(prefEx == null) {
					for (Map.Entry<String, AnalysisResult[][]> entry : organizedResults.entrySet()) {
						String name = entry.getKey();
						AnalysisResult[][] exercise = entry.getValue();
						if (prefWeeks == null) {        // both are null (most work)
							for (int w = 0; w < exercise.length; w++) {
								AnalysisResult[] week = exercise[w];
								for (AnalysisResult result : week)
									ma.addOne(result, name, w);
							}
						} else {                        // prefEx == null, prefWeeks != null
							for (int i = 0; i < prefWeeks.length && prefWeeks[i] < exercise.length; i++) {
								AnalysisResult[] week = exercise[prefWeeks[i]];
								if (week != null)
									for (AnalysisResult result : week)
										ma.addOne(result, name, prefWeeks[i]);
							}
						}
					}
				}else {
					if(prefWeeks == null) {			// prefEx != null, prefWeeks == null
//						numAnalyses = prefEx.length;
						AnalysisResult[][] exercise;
						AnalysisResult[] week;
						for(int e = 0; e < prefEx.length; e++)
							if((exercise = organizedResults.get(prefEx[e])) != null)
								for(int w = 0; w < exercise.length; w++)
									if((week = exercise[w]) != null)
										for(AnalysisResult result : week)
											ma.addOne(result, prefEx[e], w);
					}else {							// neither is null
						AnalysisResult[][] exercise;
						AnalysisResult[] week;
						String name;
						int numAnalyses = Math.min(prefEx.length, prefWeeks.length);	// for uneven lists
						for(int i = 0; i < numAnalyses; i++)
							if((exercise = organizedResults.get(name = prefEx[i])) != null
									&& prefWeeks[i] < exercise.length
									&& (week = exercise[prefWeeks[i]]) != null)
								for(AnalysisResult ar : week)
									ma.addOne(ar, name, prefWeeks[i]);
					}
				}
				ma.processAll();
				AnalysisResult[] result = ma.eval();
				metaAnalysisResults.addAll(Arrays.asList(result));
			}
			organizedResults.put(META_ANALYSIS_TITLE, 
					new AnalysisResult[][] {
						metaAnalysisResults.toArray(new AnalysisResult[0])
					}
			);
		}
		return organizedResults;
	}
	
	/**
	 * Sorts the results in {@code organizedResults} 
	 * @return
	 */
	public PriorityQueue<AnalysisResult> findRelevantResults(){
		if(relevantResults == null)
			relevantResults = new PriorityQueue<>();
		if(organizedResults == null)
			return relevantResults;
		for (Map.Entry<String, AnalysisResult[][]> entry : organizedResults.entrySet()) {
			String title = entry.getKey();
			AnalysisResult[][] week = entry.getValue();
			for (AnalysisResult[] results : week)
				if (results != null)
					relevantResults.addAll(Arrays.asList(results));
		}
		relevantResultsReady = true;
		return relevantResults;
	}

	public void initRelevantResults() { relevantResults = new PriorityQueue<>(); }

	public PriorityQueue<AnalysisResult> getRelevantResults(){
		return relevantResults;
	}

	public HashMap<String, AnalysisResult[][]> getOrganizedResults(){
		return organizedResults;
	}

	// public void readRecords(MainActivity context) moved to MainActivity for coroutines

	// public void writeRecords(MainActivity context) moved to MainActivity for coroutines
	
	public void addRecord(String name, ExerciseRecord er) {
		if(data != null) {
			ArrayList<ExerciseRecord> list = data.get(name);
			if(list == null)
				list = new ArrayList<>();
			list.add(er);
			data.put(name, list);
		}
	}
	
	public static final String[] daysOfWeek = {
			"Sunday",
			"Monday",
			"Tuesday",
			"Wednesday",
			"Thursday",
			"Friday",
			"Saturday"
	};

	public static final String[] dayOfWeekInitials = {
			"S",
			"M",
			"T",
			"W",
			"R",
			"F",
			"S"
	};
	
	public static final String[] timesOfDay = {
			"night",
			"morning",
			"afternoon",
			"evening"
	};
	
	public static String formatTime(long millis) {
		if(millis < 1000)
			return millis + "ms";
		long seconds = millis / 1000;
		millis%=1000;
		if(seconds < 10)
			return seconds + "." + (millis / 100) + "s";	// e.g. 4.5s
		if(seconds < 60)
			return seconds + "s";
		long minutes = seconds / 60;
		seconds%=60;
		if(minutes < 60)
			return minutes + "m";
		long hours = minutes / 60;
		minutes%=60;
		if(hours < 24)
			return hours + "." + String.format("%02d", minutes * 100 / 60) + "h";
		long days = hours / 24;
		hours%=24;
		return days + "." + String.format("%02d", hours * 100 / 24) + "d";
	}

	public static String formatWeekIndex(int w){
		switch(w){
			case 0:
				return "This week";
			case 1:
				return "Last week";
		}
		return w + " weeks ago";
	}

	public String formatWeekOfDate(int w){
		long time = curWeekStart.getTimeInMillis() - MILLISECONDS_PER_WEEK * w;
		Calendar weekStart = Calendar.getInstance();
		weekStart.setTimeInMillis(time);
		int month = weekStart.get(Calendar.MONTH) + 1;
		int day = weekStart.get(Calendar.DAY_OF_MONTH);
		return "Week of " + month + "/" + day;
	}
	
	private static boolean contains(Object[] arr, Object obj) {
		if(arr == null)
			return false;
		for(Object obj2 : arr)
			if(Objects.equals(obj2, obj))
				return true;
		return false;
	}

	public static <T> ArrayList<T> toArrayList(List<T> list){
		if(list instanceof ArrayList)
			return (ArrayList<T>) list;
		ArrayList<T> retval = new ArrayList<>(list.size());
		retval.addAll(list);
		return retval;
	}

}