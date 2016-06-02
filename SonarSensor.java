import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;

public class SonarSensor extends AbstractFilter {
	public static final float MAX_DISTANCE = 0.9133f; // 3ft]
	public static final float MIN_DISTANCE =  0.08f;
//	private static final float ALPHA = 0.5f;
//	private static final int SAMPLE_SIZE = 10;
	private float[] sample;
//	private float rollingAverage;

	public SonarSensor(SampleProvider source) {
		super(source);
		sample = new float[sampleSize()];

		// Initiate the rolling average;
//		rollingAverage = 0f;
//		for (int i = 0; i < SAMPLE_SIZE; i++) {
//			super.fetchSample(sample, 0);
//			rollingAverage += sample[0];
//		}
//		rollingAverage /= SAMPLE_SIZE;
	}
	
	public float read() {
		super.fetchSample(sample, 0);
		//rollingAverage = sample[0] + ALPHA * (rollingAverage - sample[0]);
		return sample[0];
	}
	
	public float getSpeedRatio() {
		return read() / MAX_DISTANCE;
	}
	
	public boolean objectFound() {
		return read() <= MAX_DISTANCE;
	}
	
	public boolean isInvestigatComplete() {
		return read() <= MIN_DISTANCE;
	}
}
