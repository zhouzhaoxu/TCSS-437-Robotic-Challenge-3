import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;

/**
 * This is an object for the bumper sensor. It will get the data from the sensors and returns whether 
 * it had been touched. 
 */
public class SonarSensor extends AbstractFilter {
	
	/*
	 * The max distance the sonar should sense an object.
	 */
	public static final float MAX_DISTANCE = 0.9133f; // 3ft]
	
	/*
	 * The minimal distance for the sonar to sense an object. 
	 */
	public static final float MIN_DISTANCE =  0.08f;
	
	/*
	 * This is used for saving an array of output from the sensor.
	 */
	private float[] sample;

	/**
	 * This is the constructor to initialize the Sonar Sensor.
	 * @param the port the sensor is using
	 */
	public SonarSensor(SampleProvider source) {
		super(source);
		sample = new float[sampleSize()];
	}
	
	/**
	 * Reading the data from the sonar sensor and return the first data.
	 * @return return the first data from the sensor
	 */
	public float read() {
		super.fetchSample(sample, 0);
		return sample[0];
	}
	
	/**
	 * Getting the speed ratio from the distance of the sonar to the max distance
	 * @return return the ratio between the sonar distance to max distance
	 */
	public float getSpeedRatio() {
		return read() / MAX_DISTANCE;
	}
	
	/**
	 * Checking if an object is found within the max distance
	 * @return if an object is found within the max distance
	 */
	public boolean objectFound() {
		return read() <= MAX_DISTANCE;
	}
	
	/**
	 * Checking if the the robot had reach the object.
	 * @return if the the robot had reach the object
	 */
	public boolean isInvestigatComplete() {
		return read() <= MIN_DISTANCE;
	}
}
