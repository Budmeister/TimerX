package analysis;

import com.timerx.thePackage.ExerciseRecord;

public class LongestSessionAnalysis implements ParallelAnalysis {

	public LongestSessionAnalysis(int weekNumber){
		this.weekNumber = weekNumber;
	}
	
	private long longestSessionTime;
	private long longestSessionLength;
	private int weekNumber;
	private String name;

	@Override
	public AnalysisResult[] eval() {
		return new AnalysisResult[] {
				new LongestSessionAnalysisResult(name, relevance(), weekNumber, longestSessionLength, longestSessionTime)
		};
	}

	@Override
	public int relevance() {
//		return (int) (-longestSessionTime * 7 / DataProcessor.MILLISECONDS_PER_HOUR);
		return 4;
	}

	@Override
	public void addOne(ExerciseRecord er) {
		long length = er.length();
		if(length > longestSessionLength) {
			longestSessionLength = length;
			longestSessionTime = er.getStartTime();
		}
		name = er.getTitle();
	}

}
