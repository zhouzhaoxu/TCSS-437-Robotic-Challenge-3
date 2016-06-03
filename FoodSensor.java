import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;

/**
 * This is an object for the bumper sensor. It will get the data from the sensors and returns whether 
 * it had been touched. 
 */
public class FoodSensor extends AbstractFilter {
	
	/*
	 * A constant for when it is white
	 */
	private static final float REFLECT_WHITE = 0.20f;
	
	/*
	 * This is used for saving an array of output from the sensor.
	 */
	private float[] sample; // the readings from the sensor
	
	/**
	 * This is the constructor to initialize the Food Sensor.
	 * @param the port the sensor is using
	 */
	public FoodSensor(SampleProvider source) {
		super(source);
		sample = new float[sampleSize()];
	}
	
	/**
	 * Reading the data from the food sensor and return the first data.
	 * @return return the first data from the sensor
	 */
	public float read() {
		super.fetchSample(sample, 0);
		return sample[0];
	}
	
	/**
	 * Tell when it is on white.
	 * @return if it is on white
	 */
	public boolean onWhite() {
		return read() >= REFLECT_WHITE;
	}
}
