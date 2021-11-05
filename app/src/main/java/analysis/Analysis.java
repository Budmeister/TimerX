package analysis;

/**
 * Represents a type of analysis that can be done on Exercise data.
 * It should be able to return its results via {@code eval()} in the form
 * of an {@code AnalysisResult} object.
 */
public interface Analysis {

	/**
	 * An array containing the results of this analysis.
	 * @return the output of the analysis.
	 */
	public AnalysisResult[] eval();

	/**
	 * Returns the relevance of this analysis. Usually, this reveals
	 * how closely the data fits this type of analysis. Lower relevance
	 * is more relevant (because it will be stored in a heap later).
	 * @return the relevance of the analysis of this data.
	 */
	public int relevance();

}