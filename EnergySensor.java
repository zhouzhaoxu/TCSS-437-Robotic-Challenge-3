import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class EnergySensor {
	private static final float LIFE_TIME = 4; // In minutes
	private static final float MAX_ENERGY = LIFE_TIME * 60;
	private static final int DECREMENT_TIMER = 1000; // One second
	public static final int MAX = -2, FULL = -1, NOT_HUNGRY = 0, HUNGRY = 1, STARVING = 2, DEAD = 3;
	public float energy;
	private boolean isFeeding;
	private boolean isDead;
	private Timer hungerTimer;
	
	public EnergySensor() {
		energy = MAX_ENERGY;
		isFeeding = false;
		isDead = false;
		hungerTimer = new Timer(DECREMENT_TIMER, new MyTimerListener());
	}
	
	public void start() {
		hungerTimer.start();
	}
	
	public float getEnergy() {
		return energy;
	}
	
	public boolean isFeeding() {
		return isFeeding;
	}
	
	public boolean isDead() {
		return isDead;
	}
	
	public void setFeeding(boolean value) {
		isFeeding = value;
	}
	
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
//		return (int)(energy/MAX_ENERGY*100);
	}
	
	private class MyTimerListener implements TimerListener {
		@Override
		public void timedOut() {
			if (!isDead) {
				if (energy == MAX_ENERGY) {
					setFeeding(false);
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
