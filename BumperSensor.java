import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;

public class BumperSensor extends AbstractFilter {
	private float[] sample;
	
	public BumperSensor(SampleProvider source) {
		super(source);
		sample = new float[sampleSize()];
	}
	
	public boolean isTouched() {
		return read() != 0f;
	}
	
	public float read() {
		super.fetchSample(sample, 0);
		//System.out.println(sample[0]);
		return sample[0];
	}
}
