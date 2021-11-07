package analysis;

import com.timerx.thePackage.DataProcessor;
import com.timerx.thePackage.ExerciseRecord;

import java.util.Calendar;
import java.util.Date;

public class TotalLengthAnalysis implements ParallelAnalysis{

	public TotalLengthAnalysis(int weekNumber){
		this.weekNumber = weekNumber;
	}
	
	private long totalLength;
	private long[] lengthEachDay = new long[7];	// Sunday through Saturday
	private int longestDay;
	private long[] lengthEachTimeOfDay = new long[4];	// night, morning, afternoon, evening
	private int longestTimeOfDay;
	private String name;
	private int weekNumber;

	@Override
	public AnalysisResult[] eval() {
		// find longest day
		long lengthOfLongestDay = 0;
		for(int d = 0; d < 7; d++)
			if(lengthEachDay[d] > lengthOfLongestDay) {
				lengthOfLongestDay = lengthEachDay[d];
				longestDay = d;
			}
		
		// find longest time of day
		long lengthOfLongestTimeOfDay = 0;
		for(int t = 0; t < 4; t++)
			if(lengthEachTimeOfDay[t] > lengthOfLongestTimeOfDay) {
				lengthOfLongestTimeOfDay = lengthEachTimeOfDay[t];
				longestTimeOfDay = t;
			}
		
		return new AnalysisResult[] {
				new TotalLengthAnalysisResult(name, totalLengthRelevance(), weekNumber, totalLength),
				new LengthEachDayAnalysisResult(name, lengthEachDayRelevance(), weekNumber, lengthEachDay, longestDay, totalLength / 7),
				new TimeOfDayAnalysisResult(name, timeOfDayRelevance(), weekNumber, lengthEachTimeOfDay, longestTimeOfDay)
		};
	}

	@Override
	public int relevance() {
		return totalLengthRelevance();
	}
	
	public int totalLengthRelevance() {
		// This is the baseline for relevance. Other relevances should
		// be calculated according to whether they are more or less relevant
		// than doing x hours of work.
		return (int) (-totalLength / DataProcessor.MILLISECONDS_PER_HOUR);
	}
	
	public int lengthEachDayRelevance() {
		return (int) (-totalLength * 12 / 10 / DataProcessor.MILLISECONDS_PER_HOUR);
	}
	
	public int timeOfDayRelevance() {
		return (int) (-lengthEachTimeOfDay[longestTimeOfDay] * 4 / DataProcessor.MILLISECONDS_PER_HOUR);
	}

	@Override
	public void addOne(ExerciseRecord er) {
		// total length
		long length = er.length();
		totalLength+=length;
		
		// sort into day
		Calendar c = Calendar.getInstance();
		Date d = new Date(er.mid());
		c.setTime(d);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK)-1;	// 1 indexed
		System.out.println(dayOfWeek + " " + length + " " + d);
		lengthEachDay[dayOfWeek]+=length;
		
		// sort into time of day
		int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		lengthEachTimeOfDay[hourOfDay / 6]+=length;
		
		name = er.getTitle();
	}

}
