package analysis;

import com.timerx.thePackage.ExerciseRecord;

/**
 * Represents a type of analysis which can be calculated as records are encountered.
 *
 */
public interface ParallelAnalysis extends Analysis{
	
	void addOne(ExerciseRecord er);

}
