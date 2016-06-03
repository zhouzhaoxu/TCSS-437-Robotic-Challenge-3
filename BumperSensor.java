import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;

/**
 * This is an object for the bumper sensor. It will get the data from the sensors and returns whether 
 * it had been touched. 
 */
public class BumperSensor extends AbstractFilter {
	
	/*
	 * This is used for saving an array of output from the sensor.
	 */
	private float[] sample;
	
	/**
	 * This is the constructor to initialize the BumperSeneor.
	 * @param source the port for which the sensor is connected to
	 */
	public BumperSensor(SampleProvider source) {
		super(source);
		sample = new float[sampleSize()];
	}
	
	/**
	 * This is used for whether the touch sensor had been touched.
	 * @return whether the touch sensor had been touched
	 */
	public boolean isTouched() {
		return read() != 0f;
	}
	
	/**
	 * Reading the data from the touch sensor and return the first data.
	 * @return the first data from the touch sensor
	 */
	public float read() {
		super.fetchSample(sample, 0);
		return sample[0];
	}
}
