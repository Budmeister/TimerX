package analysis;

import java.util.HashMap;

public class LengthEachDayMetaAnalysis implements MetaAnalysis {
	
	private String[] prefEx;
	private int[] prefWeeks;
	
	// for analysis result
	private int[] longestDay;
	private HashMap<String, long[]>[] lengths;
	private long[][] dayLengths;
	private long[] avgLengthPerDay;
	
	public LengthEachDayMetaAnalysis(String[] prefEx) {
		this(prefEx, 5);
	}
	
	@SuppressWarnings("unchecked")
	public LengthEachDayMetaAnalysis(String[] prefEx, int numWeeks) {
		this.prefEx = prefEx;
		prefWeeks = new int[numWeeks];
		longestDay = new int[numWeeks];
		lengths = new HashMap[numWeeks];
		dayLengths = new long[numWeeks][];
		avgLengthPerDay = new long[numWeeks];
		for(int w = 0; w < numWeeks; w++) {
			prefWeeks[w] = w;
			lengths[w] = new HashMap<>();
		}
	}

	@Override
	public AnalysisResult[] eval() {
		return new AnalysisResult[] {
				new LengthEachDayMetaAnalysisResult(null, relevance(), (prefWeeks.length != 0 ? prefWeeks[0] : 0), lengths, dayLengths, longestDay, avgLengthPerDay)
		};
	}

	@Override
	public int relevance() {
		return 2;
	}

	@Override
	public void addOne(AnalysisResult result, String exercise, int week) {
		if(!(result instanceof LengthEachDayAnalysisResult))
			return;
		LengthEachDayAnalysisResult ledaResult = (LengthEachDayAnalysisResult) result;
		lengths[week].put(exercise, ledaResult.lengthEachDay);
//		longestDay[week] = ledaResult.longestDay;
		long[] days = dayLengths[week];
		if(days == null)
			days = new long[7];
		for(int d = 0; d < 7; d++)
			days[d]+=ledaResult.lengthEachDay[d];
		dayLengths[week] = days;
	}

	@Override
	public void processAll() {
		for(int w = 0; w < dayLengths.length; w++) {
			if(dayLengths[w] != null) {
				avgLengthPerDay[w] = 0;
				int longestDay = 0;
				long longestLength = 0;
				for(int d = 0; d < 7; d++) {
					if(dayLengths[w][d] > longestLength) {
						longestDay = d;
						longestLength = dayLengths[w][d];
					}
					avgLengthPerDay[0]+=dayLengths[w][d];
				}
				this.longestDay[w] = longestDay;
				avgLengthPerDay[w]/=7;
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
