import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.LED;
import lejos.hardware.Sound;
import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;

public class Challenge3 extends Thread {
	
	/*
	 * Wander Constants
	 */	
	//If set to 50, then it has even odds of veering right or left.
	public final static int VEER_RIGHT_CHANCE  = 50;
	//How much to offset the odds of veering right upon each time we veer right without veering left.
	public final static int DISTRO_OFFSET = 10;
	//How much faster a motor should go if the car is veering in that direction while wandering.
	public final static int VEER_SPEED_OFFSET = 150;
	//The minimum amount of time that the robot will wander before it chooses a new direction.
	public final static int MIN_WANDER_TIME = 1000;
	//The maximum amount of time that the robot will wander before it chooses a new direction.
	public final static int MAX_WANDER_TIME = 2000;
	
	/*The default speed of the robot.*/
    public final static int DEFAULT_SPEED = 300;
	
	/* Maximum speed */  
	private static final int MAX_SPEED = 900;
	
	/* Names of all bricks */
	private static final String[] NAMES = {"g8ev3", "EV3"};
		
	/* Array of brick objects*/
	private final static Brick[] BRICKS = new Brick[NAMES.length];
	
	/*right motor*/
	private static RMIRegulatedMotor rightMotor;
	
	/*left motor*/
	private static RMIRegulatedMotor leftMotor;
	
	/*chopper motor*/
	private static RMIRegulatedMotor getToTheChopper;
	
		
	/*List of all motors*/
	private static ArrayList<RMIRegulatedMotor> motorList = new ArrayList<RMIRegulatedMotor>();
	
	/*Map of all sensors*/
	private static Map<String, BaseSensor> sensorMap = new HashMap<String, BaseSensor>();
	
	/*Toggle for the while loop*/ 
	private static boolean running = true;
	
	/*The fear sensor*/
	private static FearSensor fearSensor;
	
	/*The sonar sensor*/
	private static SonarSensor sonarSensor;
	
	/*The left bumper sensor*/
	private static BumperSensor leftBumper;
	
	/*The right bumper sensor*/
	private static BumperSensor rightBumper;
	
	/*The left color sensor for food*/
	private static FoodSensor leftFoodSensor;
	
	/*The right color sensor for food*/
	private static FoodSensor rightFoodSensor;
	
	/*The energy sensor*/
	private static EnergySensor energySensor;
	
	/*The start time for wandering*/
	private static long startTime = System.currentTimeMillis();
	
	/*The direction should the robot go while wandering*/
	private static int directionDistro = 0;
	
	/*The next wander time*/
	private static int nextWanderTime = 0;
	
	/*A check to see if it had seen white*/
	private static boolean checkWhite = false;
	
	/*Keep track of when last seen black*/
	private static long lastSeenBlack;
	
	/**
	 * main class
	 */
	public static void main(String[] args) {
		
		//long startTime = System.currentTimeMillis();
		exitProgram();

		try {			
			// Add sensors to the list
			initializeSensors1();
			initializeSensors0();
						
			energySensor= new EnergySensor();
			
			//thread for getting sensors data
			Challenge3 sensorThread = new Challenge3();
			sensorThread.start();
			
			BRICKS[1].getTextLCD().clear();
			//get light
			//0: turn off button lights
			//1/2/3: static light green/red/orange
			//4/5/6: normal blinking light green/red/yellow
			//7/8/9: fast blinking light green/red/yellow
			//>9: same as 9.
			LED[] leds = initializeLight();
			
			energySensor.start();
			
			//running the program
			while(running) {
				getToTheChopper.setSpeed(400);
				getToTheChopper.forward();
				//Showing what state the robot energy level is on the robot
				switch(energySensor.getEnergyState()) {
					case EnergySensor.NOT_HUNGRY: 
						if(!energySensor.isFeeding()) { 
							leds[1].setPattern(1); 
						} else  {
							leds[1].setPattern(7);
						}
						break;
					case EnergySensor.HUNGRY:
						if(!energySensor.isFeeding()) { 
							leds[1].setPattern(3); 
						} else  {
							leds[1].setPattern(9);
						}
						break;
					case EnergySensor.STARVING:
						if(!energySensor.isFeeding()) { 
							leds[1].setPattern(2); 
						} else  {
							leds[1].setPattern(8);
						}
						break;
					case 3:leds[1].setPattern(0); break;
				}
				
				//checking if it is dead
				if(energySensor.getEnergyState() == EnergySensor.DEAD) {
					leds[0].setPattern(0);
					leds[1].setPattern(0);
					stopMotor();
				//bumper
				} else if(leftBumper.isTouched() || rightBumper.isTouched()) {
					leds[0].setPattern(5);
					bumperHit(leftBumper.isTouched(), rightBumper.isTouched());
				
				//fear
				} else if(fearSensor.isStartled() && energySensor.getEnergyState() 
						!= EnergySensor.STARVING) {
					energySensor.setFeeding(false);
					leds[0].setPattern(3);
					runAway(fearSensor);
				//sonar
				} else if(sonarSensor.objectFound() && energySensor.getEnergyState() 
						!= EnergySensor.STARVING) {
					energySensor.setFeeding(false);
					leds[0].setPattern(4);
					investigate(sonarSensor);
				//feeding
				} else if (energySensor.isFeeding()) {
					setMotor(0);
					gradientFollowing(energySensor);
					checkWhite = false;
				//feeding
				} else if (checkWhite || (rightFoodSensor.onWhite() || leftFoodSensor.onWhite())
							&& energySensor.getEnergyState() >= EnergySensor.HUNGRY) {
					if(!checkWhite) {
						stopMotor();
						checkWhite = true;
					}
					setMotor(0);
					gradientFollowing(energySensor);
				//wander
				} else {
					leds[0].setPattern(1);
					wander(0);
				}
			}
			
			closePorts();
		} catch (Exception e) {
			closePorts();
			e.printStackTrace();
		}
	}
	
	/**
	 *  Close all the ports for all the bricks
	 */
	public static void closePorts() {
		running = false;
		for(BaseSensor s: sensorMap.values()) {
			s.close();
		}
		for(RMIRegulatedMotor m: motorList) {
			try {
				m.stop(false);
				m.close();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Connects the two bricks via bluetooth. Prints to brick display once
	 * an connection is established.
	 */
	public static void brickConnect() 
			throws RemoteException, MalformedURLException, NotBoundException {
		for(int i = 0; i < BRICKS.length; i++) {            
            BRICKS[i] = new RemoteEV3(BrickFinder.find(NAMES[i])[0].getIPAddress());
            System.out.println("Connected to " + NAMES[i]);
        }
	}
	
	/**
	 * Exit the program by closing all the ports of the sensors and motors.
	 */
	public static void exitProgram() {
		Button.ENTER.addKeyListener(new KeyListener() {

			/**
			 * listening when key is pressed
			 */
			@Override
			public void keyPressed(Key k) {
				running = false;
				System.out.println("enter press");
			}

			/**
			 * listening when key is released
			 */
			@Override
			public void keyReleased(Key k) {
				running = false;
				System.out.println("enter released");
			}
			
		});
	}
	
	/**
	 * Initialize the led light from both bricks.
	 */
	public static LED[] initializeLight() {
		LED[] leds = new LED[BRICKS.length];
		for(int i = 0; i < BRICKS.length; i++) {
            leds[i] = BRICKS[i].getLED();
		}	
		return leds;
	}
	
	/**
	 * Initialize all the sensors from brick one
	 */
	public static void initializeSensors1() throws Exception {
		
		BRICKS[1] = new RemoteEV3("10.0.1.1");
        System.out.println("Connected to " + NAMES[1]);
        
        // set motors
		rightMotor = ((RemoteEV3) BRICKS[1]).createRegulatedMotor("B", 'L');
		leftMotor = ((RemoteEV3) BRICKS[1]).createRegulatedMotor("C", 'L');
		getToTheChopper = ((RemoteEV3) BRICKS[1]).createRegulatedMotor("D", 'L');
		
		//left touch
		EV3TouchSensor leftTouch  = new EV3TouchSensor(BRICKS[1].getPort("S2")); 	
		sensorMap.put("LeftTouch", leftTouch);
		leftBumper = new BumperSensor(leftTouch);
		
		// right touch
		EV3TouchSensor rightTouch  = new EV3TouchSensor(BRICKS[1].getPort("S3")); 	
		sensorMap.put("RightTouch", rightTouch);
		rightBumper = new BumperSensor(rightTouch);

		// fear sensor
		EV3ColorSensor fearColorSensor  = new EV3ColorSensor(BRICKS[1].getPort("S1"));
		sensorMap.put("Fear", fearColorSensor); // Add to list of sensors	
		fearColorSensor.setCurrentMode("Ambient"); // set the sensor mode
		fearSensor = new FearSensor(fearColorSensor);
	}
	
	/**
	 * Initialize all the sensors from brick zero
	 */
	private static void initializeSensors0() 
			throws RemoteException, MalformedURLException, NotBoundException {
		BRICKS[0] = BrickFinder.getLocal(); 
		
		// Add motors to list
		motorList.add(rightMotor);
		motorList.add(leftMotor);
		motorList.add(getToTheChopper);
		
		//ultrasonic sensor
		EV3UltrasonicSensor sonar  = new EV3UltrasonicSensor(BRICKS[0].getPort("S4")); 	
		sensorMap.put("Distance", sonar);
		sonarSensor = new SonarSensor(sonar);
		
		//right food sensor
		EV3ColorSensor rightFoodColorSensor  = new EV3ColorSensor(BRICKS[0].getPort("S2"));
		sensorMap.put("RightFood", rightFoodColorSensor);
		rightFoodColorSensor.setCurrentMode("Red");
		rightFoodSensor = new FoodSensor(rightFoodColorSensor);
		
		//left food sensor
		EV3ColorSensor  leftFoodColorSensor  = new EV3ColorSensor(BRICKS[0].getPort("S3"));
		sensorMap.put("LeftFood", leftFoodColorSensor);
		leftFoodColorSensor.setCurrentMode("Red");
		leftFoodSensor = new FoodSensor(leftFoodColorSensor);
	}
	
	/**
	 * Wader Code
	 */
	public static int randomBiasedWalk(int directionDistro, int feedingSpeedDecrease) 
			throws RemoteException {
		/*
		 * Starting with 50:50 odds, randomly determine if the robot will veer right or left
		 * for a random amount of time.
		 * If it veers right, the odds become 60:40 in left's favor. If it veers right again,
		 * the odds become 70:30. If it then veers left, the odds become 60:40, and so on.
		 */
		 if ((int) Math.random() * 4 > directionDistro) {
			 moveForward(DEFAULT_SPEED + VEER_SPEED_OFFSET - feedingSpeedDecrease, 
					 DEFAULT_SPEED - feedingSpeedDecrease);
			 return ++directionDistro;
		 } else {
			 moveForward(DEFAULT_SPEED - feedingSpeedDecrease,
					 DEFAULT_SPEED + VEER_SPEED_OFFSET -feedingSpeedDecrease);	
			 return --directionDistro;
		 }
	}
	
	/**
	 * Wader Code with decrease speed
	 */
	public static void wander(int feedingSpeedDecrease) throws RemoteException {
		if ((System.currentTimeMillis() - startTime) > nextWanderTime) {
			startTime = System.currentTimeMillis();
			//No obstacle or line detected. Wander.
			directionDistro = randomBiasedWalk(directionDistro, feedingSpeedDecrease);
			nextWanderTime = (int) ((Math.random() * (MAX_WANDER_TIME - MIN_WANDER_TIME)) 
					+ MIN_WANDER_TIME);
	    }
	}
	
	/**
	 * Set the motor speed and move forward
	 */
	public static void moveForward(int velocity_left, int velocity_right) throws RemoteException {		
		rightMotor.setAcceleration(MAX_SPEED);
		leftMotor.setAcceleration(MAX_SPEED);
		rightMotor.setSpeed(velocity_right);
	    leftMotor.setSpeed(velocity_left);
	    rightMotor.forward();
	    leftMotor.forward();
	}
	
	/**
	 * Set the motor speed and move backward
	 */
	public static void moveBack(int velocity_left, int velocity_right) throws RemoteException {		
		rightMotor.setAcceleration(MAX_SPEED);
		leftMotor.setAcceleration(MAX_SPEED);
		rightMotor.setSpeed(velocity_right);
	    leftMotor.setSpeed(velocity_left);
	    leftMotor.backward();
	    rightMotor.backward();
	}
	
	/**
	 * Turn the robot around in a random direction
	 */
	public static void turnAround(String sensor) throws RemoteException, InterruptedException {
	    if(Math.random() * 2 < 1) {
		    rightMotor.forward();
		    leftMotor.backward();
	    } else {
	    	rightMotor.backward();
		    leftMotor.forward();
	    }
	    sleep((int) (Math.random() * 500) + 750, sensor);
	    stopMotor();
	}
	
	/**
	 * Stop the robot from moving
	 */
	public static void stopMotor() throws RemoteException {
		rightMotor.stop(true);
	    leftMotor.stop(false); 
	}
	
	/**
	 * Point turn left for the robot
	 */
	public static void pointTurnLeft() throws RemoteException {
		leftMotor.stop(true);
		rightMotor.backward();
	    leftMotor.forward();
	}
	
	/**
	 * Point turn right for the robot
	 */
	public static void pointTurnRight() throws RemoteException {
		rightMotor.stop(true);
	    leftMotor.backward();
	    rightMotor.forward();
	}
	
	/**
	 * Turn left for the robot
	 */
	public static void turnLeft() throws RemoteException {
		leftMotor.stop(true);
		rightMotor.stop(true);
	    leftMotor.forward();
	}
	
	/**
	 * Turn right for the robot
	 */
	public static void turnRight() throws RemoteException {
		rightMotor.stop(true);
	    leftMotor.stop(true);
	    rightMotor.forward();
	}
	
	/**
	 * set the motor speed with a decrease speed from the default.
	 */
	public static void setMotor(int decrease_speed) throws RemoteException {
		rightMotor.setAcceleration(DEFAULT_SPEED - decrease_speed);
		leftMotor.setAcceleration(DEFAULT_SPEED - decrease_speed);
		rightMotor.setSpeed(DEFAULT_SPEED - decrease_speed);
	    leftMotor.setSpeed(DEFAULT_SPEED - decrease_speed);
	}
	
	/**
	 * Fear behavior for running away 
	 */
	public static void runAway(FearSensor fear) throws RemoteException, InterruptedException {
		moveBack(MAX_SPEED / 2, MAX_SPEED / 2);
	    sleep(1000, "fear");
		turnAround("fear");
		int runTime = (int)(3000.0 * (fear.getFearPercent() + 0.25) + 1000);
		moveForward((int)((fear.getFearPercent() + 0.25) * MAX_SPEED) + 100, 
				(int)((fear.getFearPercent() + 0.25) * MAX_SPEED) + 100);
		sleep(runTime, "fear", fear);
		stopMotor();
		fear.resetStartled();
	}
	
	/**
	 * Sonar behavior for moving closer to an object and turn away. 
	 */
	public static void investigate(SonarSensor sonar) throws RemoteException, InterruptedException {
		//stop at 15 cm. it use the meter.
		if(sonar.isInvestigatComplete()) {
			stopMotor();
			sleep(2000, "Distance");
			moveBack(MAX_SPEED / 2, MAX_SPEED / 2);
		    sleep(1000, "Distance");
			turnAround("Distance");
		} else {
			moveForward((int)(MAX_SPEED * sonar.getSpeedRatio() * sonar.getSpeedRatio()) + 50, 
					(int)(MAX_SPEED * sonar.getSpeedRatio() * sonar.getSpeedRatio() + 50));
		}
			
	}
	
	/**
	 * Bumper behavior for when bumper hit
	 */
	public static void bumperHit(boolean left, boolean right) 
			throws RemoteException, InterruptedException {
		if(left && right) {
			stopMotor();
			Sound.beep();
			moveBack(MAX_SPEED / 2, MAX_SPEED / 2);
			sleep(1000, "Bumper");
			stopMotor();
			sleep(2000, "Bumper");
			turnAround("Bumper");
		} else if(left) {
			stopMotor();
			moveBack(MAX_SPEED / 2, MAX_SPEED / 2);
			sleep(500, "Bumper");
			stopMotor();
			pointTurnRight();
		} else {
			stopMotor();
			moveBack(MAX_SPEED / 2, MAX_SPEED / 2);
			sleep(500, "Bumper");
			stopMotor();
			pointTurnLeft();
		}
	}
	
	/**
	 * Feeding behavior for when the color sensor detects white
	 */
	public static void gradientFollowing(EnergySensor energySensor) 
			throws RemoteException, InterruptedException {
		if (System.currentTimeMillis() - lastSeenBlack >= 2000 && !energySensor.isFeeding()) {
			energySensor.setFeeding(true);
			Sound.beep();
		}
		// Get both food sensors onto white.
		if (leftFoodSensor.onWhite() && !rightFoodSensor.onWhite()) {
			turnRight();
		} else if (rightFoodSensor.onWhite() && !leftFoodSensor.onWhite()) {
			turnLeft();
		} else if (!rightFoodSensor.onWhite() && !leftFoodSensor.onWhite()) {
			// Neither is on white.
			setMotor(200);
			pointTurnRight();	
		} else { // Both on white
			wander(150);
		}
	}
	
	/**
	 * A sleep function for the motor on how long it should run without needing bumper.
	 */
	public static void sleep(int milliseconds, String sensor) 
			throws RemoteException, InterruptedException {
		long time = (long) (System.currentTimeMillis() + milliseconds);
		boolean exitSleep = false;
		sensor = sensor.toLowerCase();
		while (!exitSleep && System.currentTimeMillis() < time) {
			// Update exitSleep value with sensor values.
			if(!sensor.equals("bumper")){ 
				if(!sensor.equals("fear")) exitSleep |= fearSensor.isStartled();
				if(!sensor.equals("distance") && !sensor.equals("fear")) 
					exitSleep |= sonarSensor.objectFound();
			}
		}
	}
	
	/**
	 * A sleep function for the motor on how long it should run with using bumper.
	 */
	public static void sleep(int milliseconds, String sensor, FearSensor fear) 
			throws RemoteException, InterruptedException {
		long time = (long) (System.currentTimeMillis() + milliseconds);
		boolean exitSleep = false;
		sensor = sensor.toLowerCase();
		long stopTime = 0;
		long endTime = 0;
		while (!exitSleep && System.currentTimeMillis() < time + (endTime - stopTime)) {
			// Update exitSleep value with sensor values.
			if(!sensor.equals("bumper")){ 
				if(leftBumper.isTouched() || rightBumper.isTouched()) {
					stopTime = System.currentTimeMillis();
					bumperHit(leftBumper.isTouched(), rightBumper.isTouched());
					endTime = System.currentTimeMillis();
					moveForward((int)((fear.getFearPercent() + 0.25) * MAX_SPEED) + 100, 
							(int)((fear.getFearPercent() + 0.25) * MAX_SPEED) + 100);
				}
				if(!sensor.equals("fear")) exitSleep |= fearSensor.isStartled();
				if(!sensor.equals("distance") && !sensor.equals("fear")) 
					exitSleep |= sonarSensor.objectFound();
			}
		}
	}
	
	/**
	 * Another thread to get the sensor data
	 */
	public void run() {
		while(running) {
			BRICKS[1].getTextLCD().drawString("Energy Level: " + energySensor.getEnergy(),  0, 3);
			BRICKS[1].getTextLCD().drawString("Feeding: " + energySensor.isFeeding(), 0, 4);
			BRICKS[1].getTextLCD().drawString("Fear status: " + fearSensor.isStartled(), 0, 5);		
			if (!rightFoodSensor.onWhite() || !leftFoodSensor.onWhite()) {
				if(!rightFoodSensor.onWhite()) {
					System.out.println("BRight");
				} else if(!leftFoodSensor.onWhite()) {
					System.out.println("BLeft");
				}
				lastSeenBlack = System.currentTimeMillis();	
			} 

			if (energySensor.getEnergyState() != EnergySensor.STARVING) {
				fearSensor.read();
			}
			
			sonarSensor.read();
		}
	}
}
