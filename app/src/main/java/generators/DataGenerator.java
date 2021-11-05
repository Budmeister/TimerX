package generators;

import java.util.Random;

import com.timerx.thePackage.DataProcessor;
import com.timerx.thePackage.ExerciseRecord;

public class DataGenerator {
	
	static Random r = new Random();
	public static final int NORMAL = 0, GAUSSIAN = 1;
	static int randomMode = GAUSSIAN;
	static long gAvg = 3 * DataProcessor.MILLISECONDS_PER_DAY, gStdDev = 3 * DataProcessor.MILLISECONDS_PER_DAY;
	static long weekStart;
	static long now;

	public static DataProcessor generateData(int mode, int week){
		return generateData(new DataProcessor(), mode, week);
	}
	
	public static DataProcessor generateData(DataProcessor dp, int mode, int week) {
		weekStart = dp.weekStart();
		now = dp.now;
		randomMode = mode;
		System.out.println("Now: " + new java.util.Date(now));
		String[] names = {
				"Calculus",
				"Statics"
		};
		
		int numRecords = 20;
		long totalTime = 0;
		long start;
		long end;
		for(int i = 0; i < numRecords; i++) {
			start = getStartTime(week);
			end = getEndTime(start);
			totalTime+=end-start;
			String name = names[r.nextInt(names.length)];
			ExerciseRecord er = new ExerciseRecord(
					name,
					start,
					end
			);
			dp.addRecord(name, er);
			System.out.println(new java.util.Date(er.mid()) + " " + new java.util.Date(er.getStartTime()));
		}

		System.out.println("Total time spent on: " + DataProcessor.formatTime(totalTime));
		return dp;
	}
	
	private static long getStartTime(int week) {
		long max = DataProcessor.MILLISECONDS_PER_WEEK;
		if(week == 0)
			max = now;
		long start = weekStart - DataProcessor.MILLISECONDS_PER_WEEK * week;
		switch(randomMode){
			case NORMAL:
				return start + getRandPosLong() % max;
			case GAUSSIAN:
				return start + getGaussian(max);
		}
		return 0;
	}
	
	private static long getEndTime(long start) {
		long max = 1000 * 60 * 60 * 4;
		return start + getRandPosLong() % max;
	}
	
	private static long getRandPosLong() {
		return r.nextLong() & 0x7fffffffffffffffl;
	}
	
	private static long getGaussian(long max) {
		long retval;
//		long max = DataProcessor.MILLISECONDS_PER_WEEK - DataProcessor.MILLISECONDS_PER_DAY;
		do{
			retval = (long) (r.nextGaussian() * gStdDev + gAvg);
		}while(retval < 0 || retval > max);
		return retval;
	}

}
