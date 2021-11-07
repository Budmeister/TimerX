package generators;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.timerx.thePackage.DataProcessor;
import com.timerx.thePackage.ExerciseRecord;

public class DataGenerator {
	
	private static Random r = new Random();
	public static final int NORMAL = 0, GAUSSIAN = 1, DAY_TIME = 2;
	public static int randomMode = NORMAL;
	public static long gAvg = 3 * DataProcessor.MILLISECONDS_PER_DAY, gStdDev = 3 * DataProcessor.MILLISECONDS_PER_DAY;
	public static long now;
	public static long minStart;

	public static HashMap<String, Integer> generateData(int mode){
		return generateData(new DataProcessor(), mode);
	}
	
	public static HashMap<String, Integer> generateData(DataProcessor dp, int mode) {
		now = dp.now;
		minStart = dp.getWeekStart(1);
		randomMode = mode;
		Log.i("DataGenerator", "Now: " + new java.util.Date(now));
		String[] names = {
				"Calculus",
				"Statistics",
				"Physics"
		};
		Integer[] colorNums = {
				0xffeda334,
				0xff4287f5,
				0xff1b169e
		};
		
		int numRecords = 40;
		long totalTime = 0;
		long start;
		long end;
		ArrayList<ExerciseRecord> records = new ArrayList<>();
		for(int i = 0; i < numRecords; i++) {
			do {
				start = getStartTime();
				end = getEndTime(start);
			} while(collides(records, start, end));
			totalTime+=end-start;
			String name = names[r.nextInt(names.length)];
			ExerciseRecord er = new ExerciseRecord(
					name,
					start,
					end
			);
			records.add(er);
			Log.d("DataGenerator", name + ": " + new java.util.Date(er.getStartTime()) + " - " + new java.util.Date(er.getEndTime()));
		}
		for(ExerciseRecord er : records)
			dp.addRecord(er.getTitle(), er);

		System.out.println("Total time spent: " + DataProcessor.formatTime(totalTime));

		HashMap<String, Integer> colors = new HashMap<>();
		for(int i = 0; i < names.length; i++)
			colors.put(names[i], colorNums[i]);
		return colors;
	}

	private static boolean collides(ArrayList<ExerciseRecord> records, long start, long end){
		for(ExerciseRecord er : records)
			if(start <= er.getEndTime() && er.getStartTime() <= end)
				return true;
		return false;
	}
	
	private static long getStartTime() {
		long max = now - minStart;
		switch(randomMode){
			case NORMAL:
				return minStart + getRandPosLong() % max;
			case GAUSSIAN:
				return minStart + getGaussian(max);
			case DAY_TIME:
				return getStartDayTime();
		}
		return 0;
	}
	
	private static long getEndTime(long start) {
		long max = 1000 * 60 * 60 * 2;
		return start + getRandPosLong() % max;
	}
	
	private static long getRandPosLong() {
		return r.nextLong() & 0x000fffffffffffffl;
	}
	
	private static long getGaussian(long max) {
		long retval;
//		long max = DataProcessor.MILLISECONDS_PER_WEEK - DataProcessor.MILLISECONDS_PER_DAY;
		do{
			retval = (long) (r.nextGaussian() * gStdDev + gAvg);
		}while(retval < 0 || retval > max);
		return retval;
	}

	private static long getStartDayTime(){
		int numDays = (int) ((now - minStart) / DataProcessor.MILLISECONDS_PER_DAY + 1);
		long stdDev = DataProcessor.MILLISECONDS_PER_HOUR * 3;
		long avg = DataProcessor.MILLISECONDS_PER_HOUR * 15;
		long retval;
		do{
			retval = minStart + r.nextInt(numDays) * DataProcessor.MILLISECONDS_PER_DAY +
					(long) (r.nextGaussian() * stdDev + avg);
		} while(retval < minStart || retval > now);
		return retval;
	}

}
