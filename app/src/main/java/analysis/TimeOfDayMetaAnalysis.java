package analysis;

import java.util.HashMap;

public class TimeOfDayMetaAnalysis implements MetaAnalysis {
	
	private String[] prefEx;
	private int[] prefWeeks;
	
	// for analysis result
	private int[] longestTime;
	private HashMap<String, long[]>[] lengths;
	private long[][] timeLengths;
	
	
	public TimeOfDayMetaAnalysis(String[] prefEx) {
		this(prefEx, 5);
	}
	
	@SuppressWarnings("unchecked")
	public TimeOfDayMetaAnalysis(String[] prefEx, int numWeeks) {
		this.prefEx = prefEx;
		prefWeeks = new int[numWeeks];
		longestTime = new int[numWeeks];
		lengths = new HashMap[numWeeks];
		timeLengths = new long[numWeeks][];
		for(int w = 0; w < numWeeks; w++) {
			prefWeeks[w] = w;
			lengths[w] = new HashMap<>();
		}
	}

	@Override
	public AnalysisResult[] eval() {
		return new AnalysisResult[] {
				new TimeOfDayMetaAnalysisResult(null, relevance(), (prefWeeks.length != 0 ? prefWeeks[0] : 0), lengths, timeLengths, longestTime)
		};
	}

	@Override
	public int relevance() {
		return 3;
	}

	@Override
	public void addOne(AnalysisResult result, String exercise, int week) {
		if(!(result instanceof TimeOfDayAnalysisResult))
				return;
		TimeOfDayAnalysisResult todaResult = (TimeOfDayAnalysisResult) result;
		lengths[week].put(exercise, todaResult.lengthEachTimeOfDay);
		long[] times = timeLengths[week];
		if(times == null)
			times = new long[4];	// 4 times of day (n, m, a, e) hard coded in
		for(int t = 0; t < 4; t++)
			times[t]+=todaResult.lengthEachTimeOfDay[t];
		timeLengths[week] = times;
	}

	@Override
	public void processAll() {
		for(int w = 0; w < timeLengths.length; w++) {
			if(timeLengths[w] != null) {
				int longestTime = 0;
				long longestLength = 0;
				for(int t = 0; t < 4; t++) {
					if(timeLengths[w][t] > longestLength) {
						longestTime = t;
						longestLength = timeLengths[w][t];
					}
				}
				this.longestTime[w] = longestTime;
			}
		}
	}

	@Override
	public String[] preferredExercises() {
		return prefEx;
	}

	@Override
	public int[] preferredWeeks() {
		return prefWeeks;
	}

}
