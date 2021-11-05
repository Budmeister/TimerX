package analysis;

import java.util.HashMap;
import java.util.Map;

public class TotalLengthMetaAnalysis implements MetaAnalysis {
	
	private HashMap<String, Long>[] lengths;
	private String[] prefEx;
	
	// for analysis result
	private int[] prefWeeks;
	private String[] longestExercise;
	
	public TotalLengthMetaAnalysis(String[] prefEx) {
		this(prefEx, 5);
	}
	
	@SuppressWarnings("unchecked")
	public TotalLengthMetaAnalysis(String[] prefEx, int numWeeks) {
		this.prefEx = prefEx;
		prefWeeks = new int[numWeeks];
		lengths = new HashMap[numWeeks];
		for(int w = 0; w < numWeeks; w++) {
			prefWeeks[w] = w;
			if(prefEx == null) {
				lengths[w] = new HashMap<>();
			}else {
				lengths[w] = new HashMap<>(prefEx.length * 4 / 3);
				for(String name : prefEx)
					lengths[w].put(name, 0l);
			}
		}
	}

	@Override
	public AnalysisResult[] eval() {
		return new AnalysisResult[] {
				new TotalLengthMetaAnalysisResult(
						longestExercise.length != 0 ? longestExercise[0] : null, relevance(), (prefWeeks.length != 0 ? prefWeeks[0] : 0), lengths, longestExercise)
		};
	}

	@Override
	public int relevance() {
		return 1;
	}

	@Override
	public void addOne(AnalysisResult result, String exercise, int week) {
		if(!(result instanceof TotalLengthAnalysisResult))
			return;
		TotalLengthAnalysisResult tlaResult = (TotalLengthAnalysisResult) result;
		Long curLength = lengths[week].get(tlaResult.getTitle());
		if(curLength == null)
			curLength = 0l;
		curLength+=tlaResult.getTotalLength();
		lengths[week].put(tlaResult.getTitle(), curLength);
	}

	@Override
	public void processAll() {
		class Holder{
			String s;	// name of max exercise
			long l;		// length of max exercise
			Holder(String st, long lo) { s = st; l = lo; }
		}
		longestExercise = new String[lengths.length];
		for(int w = 0; w < lengths.length; w++) {
			Holder max = new Holder("", 0);
			for (Map.Entry<String, Long> entry : lengths[w].entrySet()) {
				String name = entry.getKey();
				Long length = entry.getValue();
				if (length > max.l) {
					max.l = length;
					max.s = name;
				}
			}
			longestExercise[w] = max.s;
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
