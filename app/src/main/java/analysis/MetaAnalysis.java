package analysis;

public interface MetaAnalysis extends Analysis {
	
	/**
	 * Runs iterative calculations on this result. This method
	 * will be called on every AnalysisResult of the desired exercise
	 * and week, so it will commonly return immediately if it is
	 * given a type of analysis it does not care about. 
	 * @param result the result to analyze
	 * @param exercise the exercise this result belongs to
	 * @param week the week of this analysis result
	 */
	void addOne(AnalysisResult result, String exercise, int week);
	
	void processAll();
	
	String[] preferredExercises();
	
	int[] preferredWeeks();

}
