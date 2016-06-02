import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;

public class FoodSensor extends AbstractFilter {
	private static final float REFLECT_WHITE = 0.20f;
	private float[] sample; // the readings from the sensor
	

	public FoodSensor(SampleProvider source) {
		super(source);
		sample = new float[sampleSize()];
	}
	
	public float read() {
		super.fetchSample(sample, 0);
		return sample[0];
	}
	
	public boolean onWhite() {
		return read() >= REFLECT_WHITE;
	}
}
