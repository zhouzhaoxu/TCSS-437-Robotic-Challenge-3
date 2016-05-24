import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

class FearSensor extends AbstractFilter {
		private static final int MAX_FEAR_ENCOUNTERS = 4;
		private static final float FEAR_THRESHOLD = (float) 0.5; // Some made up value. Needs testing.
		private static final int FEAR_RESET_TIME = 60 * 1000; // 60 seconds
		private float[] sample; // the readings from the sensor
		private int fearCount;
		private long lastFearTime;
		private Timer fearResetTimer;
		private boolean isStartled;

		public FearSensor(SampleProvider source) {
			super(source);
			isStartled = false;
			sample = new float[sampleSize()];
			fearCount = 4;
			lastFearTime = System.currentTimeMillis();
			fearResetTimer = new Timer(FEAR_RESET_TIME, new MyTimerListener()); // This calls timedOut every FEAR_RESET_TIME
			fearResetTimer.start();
		}
		
		public float read() {
			super.fetchSample(sample, 0);
			if (sample[0] >= FEAR_THRESHOLD && fearCount > 0) {
				isStartled = true;
				fearCount--;
				lastFearTime = System.currentTimeMillis();
			} else {
				isStartled = false;
			}
			return sample[0];
		}
		
		public float getFearPercent() {
			return (fearCount / MAX_FEAR_ENCOUNTERS);
		}
		
		public boolean isStartled() {
			return isStartled;
		}
		
		public void resetStartled() {
			isStartled = false;
		}
		
		private class MyTimerListener implements TimerListener {
			@Override
			public void timedOut() {
				System.out.println("Checking if we need to reset fear.");
				if (System.currentTimeMillis() - lastFearTime >= FEAR_RESET_TIME) {
					if (fearCount < 4) {
						fearCount++;
						lastFearTime = System.currentTimeMillis(); // Reset lastFearTime
					}
				}
			}
			
		}
	}