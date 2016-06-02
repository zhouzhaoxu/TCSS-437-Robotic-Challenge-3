import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class EnergySensorBackUp {
	private static final float LIFE_TIME = 1;
	private static final float MAX_ENERGY = LIFE_TIME * 60;
	private static final int DECREMENT_TIMER = 1000; // One second
	public static final int FULL = 0, HUNGRY = 1, STARVING = 2, DEAD = 3;
	private float energy;
	private boolean isFeeding;
	private boolean isDead;
	private Timer hungerTimer;
	public enum EnergyLevel {DEAD, STARVING, HUNGERY, FULL, MAX};
	
	public EnergySensorBackUp() {
		energy = MAX_ENERGY;
		isFeeding = false;
		isDead = false;
		hungerTimer = new Timer(DECREMENT_TIMER, new MyTimerListener());
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
		if (energy >= MAX_ENERGY / 2) {
			return FULL; // not interested in food
		} else if (energy >= MAX_ENERGY/4 && energy < MAX_ENERGY/2) {
			return HUNGRY; // non-deterministic
		} else if (energy > 0 && energy < MAX_ENERGY/4){
			return STARVING; // must feed 
		} else {
			return DEAD; // dead
		}
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
