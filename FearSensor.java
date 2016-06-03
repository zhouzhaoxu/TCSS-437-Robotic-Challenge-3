import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * This is an object for the fear sensor. It will keep track of how many times it had got scared and 
 * returns how the percentage of of the fear level.
 */
class FearSensor extends AbstractFilter {
		
	/*
	 * The max fear level that the robot can get scared.
	 */
	private static final float MAX_FEAR_ENCOUNTERS = 4;
	
	/*
	 * This is the light level of it needs to get scared.
	 */
	private static final float FEAR_THRESHOLD = (float) 0.45;
	
	/*
	 * This is the time when the fear will be reset.
	 */
	private static final int FEAR_RESET_TIME = 60 * 1000; // 60 seconds
	
	/*
	 * This is used for saving an array of output from the sensor.
	 */
	private float[] sample; // the readings from the sensor
	
	/*
	 * How many fear it had encounter.
	 */
	private float fearCount;
	
	/*
	 * The last fear it had encounter.
	 */
	private long lastFearTime;
	
	/*
	 * This is the reset timer for fear.
	 */
	private Timer fearResetTimer;
	
	/*
	 * This is to tell if the robot had startle or not.
	 */
	private boolean isStartled;
		
	/**
	 * This is the constructor to initialize the FearSensor.
	 * @param the port the sensor is using
	 */
	public FearSensor(SampleProvider source) {
		super(source);
		isStartled = false;
		sample = new float[sampleSize()];
		fearCount = 4;
		lastFearTime = System.currentTimeMillis();
		// This calls timedOut every FEAR_RESET_TIME
		fearResetTimer = new Timer(FEAR_RESET_TIME, new MyTimerListener()); 
		fearResetTimer.start();
	}
	
	/**
	 * Reading the data from the fear sensor and return the first data.
	 * @return return the first data from the sensor
	 */
	public float read() {
		super.fetchSample(sample, 0);
		if (sample[0] >= FEAR_THRESHOLD && fearCount > 0 && !isStartled) {
			isStartled = true;
			fearCount--;
			lastFearTime = System.currentTimeMillis();
		}
		return sample[0];
	}
	
	/**
	 * Get the fear percentage of current fear count  over max fear count.
	 * @return the fear percentage
	 */
	public float getFearPercent() {
		return (fearCount / MAX_FEAR_ENCOUNTERS);
	}
	
	/**
	 * Get the current fear count.
	 * @return the fear count
	 */
	public float getFearCounter() {
		return fearCount;
	}
	
	/**
	 * Check if it is being startled.
	 * @return if the robot is startled
	 */
	public boolean isStartled() {
		return isStartled;
	}
	
	/**
	 * Reset the startled.
	 */
	public void resetStartled() {
		isStartled = false;
	}
	
	/**
	 * A small class that acts as a timer for increment the fear count after a certain time. 
	 */
	private class MyTimerListener implements TimerListener {
		
		/**
		 * To increment the fear count after certain time.
		 */
		@Override
		public void timedOut() {
			if (System.currentTimeMillis() - lastFearTime >= FEAR_RESET_TIME) {
				if (fearCount < 4) {
					fearCount++;
					// Reset lastFearTime
					lastFearTime = System.currentTimeMillis(); 
				}
			}
		}
		
	}
}
