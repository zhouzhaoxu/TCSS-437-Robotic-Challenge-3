import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

class FearSensor extends AbstractFilter {
		private static final float MAX_FEAR_ENCOUNTERS = 4;
		private static final float FEAR_THRESHOLD = (float) 0.45; // Some made up value. Needs testing.
		private static final int FEAR_RESET_TIME = 60 * 1000; // 60 seconds
		private float[] sample; // the readings from the sensor
		private float fearCount;
		private long lastFearTime;
		private Timer fearResetTimer;
		private boolean isStartled;
		
		//source is the port
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
			if (sample[0] >= FEAR_THRESHOLD && fearCount > 0 && !isStartled) {
				isStartled = true;
				fearCount--;
				lastFearTime = System.currentTimeMillis();
			}
			return sample[0];
		}
		
		public float getFearPercent() {
			return (fearCount / MAX_FEAR_ENCOUNTERS);
		}
		
		public float getFearCounter() {
			return fearCount;
		}
		
		public boolean isStartled() {
			return isStartled;
		}
		
		public void resetStartled() {
			isStartled = false;
		}
		
		private Timer runTimer;
		private RunningTimerListener runTimerListener;
		public void startRunningTimer(int time) {
			runTimerListener = new RunningTimerListener(time);
			runTimer = new Timer(1, runTimerListener);
		}
		public void stopRunningTimer() {
			if (runTimer != null) runTimer.stop();
		}
		public void resumeRunningTimer() {
			if (runTimer != null) runTimer.start();
		}
		public boolean isRunFinish() {
			if (runTimerListener != null) {
				return runTimerListener.getRunTimeFinish();
			}
			return true;
		}
		
		public int getRemainingTime() {
			if (runTimerListener != null) return runTimerListener.remainingTime();
			return -1;
		}
		
		private class RunningTimerListener implements TimerListener {
			private int totalTime;
			private boolean runTimeFinish;
			
			public RunningTimerListener(int totalTime) {
				this.totalTime = totalTime;
				this.runTimeFinish = false;
			}

			@Override
			public void timedOut() {
				totalTime--;
				if (totalTime <= 0) {
					resetStartled();
					runTimeFinish = true;
					runTimer.stop();
				}
			}
			
			public int remainingTime() {
				return totalTime;
			}
			
			public boolean getRunTimeFinish() {
				return runTimeFinish;
			}
		}
		
		private class MyTimerListener implements TimerListener {
			@Override
			public void timedOut() {
//				System.out.println("Checking if we need to reset fear.");
				if (System.currentTimeMillis() - lastFearTime >= FEAR_RESET_TIME) {
					if (fearCount < 4) {
						fearCount++;
						lastFearTime = System.currentTimeMillis(); // Reset lastFearTime
					}
				}
			}
			
		}
	}
