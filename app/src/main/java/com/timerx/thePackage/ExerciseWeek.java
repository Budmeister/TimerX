package com.timerx.thePackage;

import java.util.ArrayList;
import java.util.Collection;

import analysis.Analysis;
import analysis.AnalysisResult;
import analysis.ParallelAnalysis;
import analysis.PostAnalysis;

/**
 * Stores and analyzes all the information for a specific exercise for a specific week
 * @author Brian Smith
 */
public class ExerciseWeek {
	
	private ArrayList<ExerciseRecord> records;
	private ArrayList<Analysis> analyses;
	
	public ExerciseWeek(){
		this(null, null);
	}
	
	public ExerciseWeek(ArrayList<ExerciseRecord> r) {
		this(r, null);
	}
	
	public ExerciseWeek(ArrayList<ExerciseRecord> r, ArrayList<Analysis> as){
		records = r;
		if(records == null)
			records = new ArrayList<>();
		analyses = as;
		if(analyses == null)
			analyses = new ArrayList<>();
	}

	public void loadRecords(Collection<? extends ExerciseRecord> rs){ records.addAll(rs); }
	
	public void addRecord(ExerciseRecord er) {
		records.add(er);
	}

	public ArrayList<ExerciseRecord> getRecords() { return records; }
	
	public void addAnalysis(Analysis a) {
		analyses.add(a);
	}
	
	public void loadAnalyses(Collection<? extends Analysis> as) {
		analyses.addAll(as);
	}
	
	public void clearAnalyses() {
		analyses = new ArrayList<>();
	}
	
	public void trimToSize() {
		records.trimToSize();
		analyses.trimToSize();
	}
	
	/**
	 * Runs the calculations on each type of analysis for this week. 
	 * @return an array containing the {@code AnalysisResult} objects from
	 * each {@code Analysis}'s {@code eval} method. This should be called
	 * after all its data has been loaded.
	 */
	public AnalysisResult[] calculateAnalyses() {
		trimToSize();
		for(ExerciseRecord er : records)
			for(Analysis a : analyses)
				if(a instanceof ParallelAnalysis)
					((ParallelAnalysis) a).addOne(er);
		ArrayList<AnalysisResult> results = new ArrayList<>();
		for(int i = 0; i < analyses.size(); i++) {
			Analysis a = analyses.get(i);
			if(a instanceof PostAnalysis)
				((PostAnalysis) a).processAll(records);
			AnalysisResult[] result = a.eval();
			for(AnalysisResult r : result)
				results.add(r);
		}
		return results.toArray(new AnalysisResult[results.size()]);
	}

}
