package analysis;

import com.timerx.thePackage.ExerciseRecord;

import java.util.ArrayList;

/**
 * Represents a type of analysis which is calculated after all the records are stored.
 *
 */
public interface PostAnalysis extends Analysis {
	
	void processAll(ArrayList<ExerciseRecord> list);

}
