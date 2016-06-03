import lejos.hardware.Sound;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * This is an object for the energy sensor. It will keep track of the energy of the robot and returns
 * what energy level it is in.
 */
public class EnergySensor {
	
	/*
	 * The total time in minute that the robot can stay alive.
	 */
	private static final float LIFE_TIME = 4;
	
	/*
	 * The maximum amount of energy the robot has in seconds.
	 */
	private static final float MAX_ENERGY = LIFE_TIME * 60;
	
	/*
	 * The amount of time to decrement. One second. 
	 */
	private static final int DECREMENT_TIMER = 1000; // One second
	
	/*
	 * Integers to represent different states of hunger. 
	 */
	public static final int MAX = -2, FULL = -1, NOT_HUNGRY = 0, HUNGRY = 1, STARVING = 2, DEAD = 3;
	
	/*
	 * Variable to hold the current amount of energy.
	 */
	private float energy;
	
	/*
	 * Boolean to determine whether it is in feeding of not. 
	 */
	private boolean isFeeding;
	
	/*
	 * Boolean to determine whether the robot is dead or not.
	 */
	private boolean isDead;
	
	/*
	 * Timer to keep track of hunger level.
	 */
	private Timer hungerTimer;
	
	/**
	 * Constructor for the energy sensor.
	 */
	public EnergySensor() {
		energy = MAX_ENERGY;
		isFeeding = false;
		isDead = false;
		hungerTimer = new Timer(DECREMENT_TIMER, new MyTimerListener());
	}
	
	/**
	 * Method to start the timer.
	 */
	public void start() {
		hungerTimer.start();
	}
	
	/**
	 * Method to get the current energy level.
	 * @return energy is the current energy level.
	 */
	public float getEnergy() {
		return energy;
	}
	
	/**
	 * Method to get the isFeeding boolean. 
	 * @return isFeeding is the current feeding status.
	 */
	public boolean isFeeding() {
		return isFeeding;
	}
	
	/**
	 * Method to get the isDead variable.
	 * @return isDead is the current life state of the robot.
	 */
	public boolean isDead() {
		return isDead;
	}
	
	/**
	 * Method to set whether the robot is feeding or not. 
	 * @param value is the new feeding status.
	 */
	public void setFeeding(boolean value) {
		isFeeding = value;
	}
	
	/**
	 * Method to return the current energy state based of the constants established earlier. 
	 * @return is the current energy state. 
	 */
	public int getEnergyState() {
		if (energy == 100) {
			return MAX;
		} else if (energy >= MAX_ENERGY - 10) {
			return FULL;
		} else if (energy >= MAX_ENERGY / 2) {
			return NOT_HUNGRY; // not interested in food
		} else if (energy >= MAX_ENERGY/4 && energy < MAX_ENERGY/2) {
			return HUNGRY; // non-deterministic
		} else if (energy > 0 && energy < MAX_ENERGY/4){
			return STARVING; // must feed 
		} else {
			return DEAD; // dead
		}
	}
	
	/**
	 * A small class that acts as a timer for increment and decrement the energy level. 
	 */
	private class MyTimerListener implements TimerListener {
		
		/**
		 * To increment and decrement the energy level.
		 */
		@Override
		public void timedOut() {
			if (!isDead) {
				if (energy == MAX_ENERGY) {
					setFeeding(false);
					//beep when the energy is full
					Sound.twoBeeps();
				}
				if (isFeeding && energy < MAX_ENERGY) {
					// Increment energy if it's feeding and hungry
					energy++;
				} else {
					// Decrement the timer by 1 if the robot is not feeding.
					energy--;
				}
			}
			isDead = energy <= 0;
		}
	}
	
}
