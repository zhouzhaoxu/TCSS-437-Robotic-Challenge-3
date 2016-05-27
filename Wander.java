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
import lejos.hardware.port.Port;
import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;

public class Challenge3 extends Thread {
	
	/** Maximum speed */  
	private static final int MAX_SPEED = 900;
	
	/** Names of all bricks */
	private static final String[] NAMES = {"g8ev3", "EV3"};
		
	/** Array of brick objects*/
	private final static Brick[] BRICKS = new Brick[NAMES.length];
	
	// Left and right motors
	private static RMIRegulatedMotor rightMotor;
	private static RMIRegulatedMotor leftMotor;
		
	//List of all motors
	private static ArrayList<RMIRegulatedMotor> motorList = new ArrayList<RMIRegulatedMotor>();
	
	//Map of all sensors
	private static Map<String, BaseSensor> sensorMap = new HashMap<String, BaseSensor>();
	
	// Toggle for the while loop 
	private static boolean running = true;
	
	private static FearSensor fearSensor;
	
	/**
	 * main class
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		exitProgram();

		try {
			brickConnect();
			
			rightMotor = ((RemoteEV3) BRICKS[1]).createRegulatedMotor("A", 'L');
			leftMotor = ((RemoteEV3) BRICKS[1]).createRegulatedMotor("D", 'L');
			
			// Add motors to list
			motorList.add(rightMotor);
			motorList.add(leftMotor);
			
			initializeSensors();
			fearSensor = new FearSensor(sensorMap.get("Fear")); 
			
			Challenge3 sensorThread = new Challenge3();
			sensorThread.start();
			
			BRICKS[1].getTextLCD().clear();
			while(running) {
				//System.out.println("" + fearSensor.read());
				
				if(fearSensor.isStartled()) {
					//runAway(fearSensor);
				} else {
//					BRICKS[1].getTextLCD().drawString("fear false",  0, 6);
				}
			}
			
			closePorts();
		} catch (Exception e) {
			//System.out.println(e.getMessage());
			closePorts();
			e.printStackTrace();
		}

		
		
		System.out.println("End of program");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 *  Close ports
	 */
	public static void closePorts() {
		running = false;
		for(BaseSensor s: sensorMap.values()) {
			s.close();
		}
		for(RMIRegulatedMotor m: motorList) {
			try {
				m.stop(true);
				m.close();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Connects the two bricks via bluetooth. Prints to brick display once
	 * an connection is established.
	 */
	public static void brickConnect() throws RemoteException, MalformedURLException, NotBoundException {
		for(int i = 0; i < BRICKS.length; i++) {
            
            BRICKS[i] = new RemoteEV3(BrickFinder.find(NAMES[i])[0].getIPAddress());
            System.out.println("Connected to " + NAMES[i]);
        }
	}
	
	public static void exitProgram() {
		Button.ENTER.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(Key k) {
				running = false;
			}

			@Override
			public void keyReleased(Key k) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	/**
	 * Initialize ports for all sensors
	 */
	public static void initializeSensors() {
		//get the port for color sensor
		Port s4 = BRICKS[0].getPort("S3"); // Get the sensor port
		// Set the type of sensor to the port.
		EV3ColorSensor fearColorSensor  = new EV3ColorSensor(s4); 		
		fearColorSensor.setCurrentMode("Ambient"); // set the sensor mode
		sensorMap.put("Fear", fearColorSensor); // Add to list of sensors	
	}
	
	public static void setMotor(int velocity_left, int velocity_right) throws RemoteException {		
		rightMotor.setAcceleration(900);
		leftMotor.setAcceleration(900);
		rightMotor.setSpeed(velocity_right);
	    leftMotor.setSpeed(velocity_left);
	    rightMotor.forward();
	    leftMotor.forward();
	}
	
	public static void turnAround() throws RemoteException, InterruptedException {
		rightMotor.setAcceleration(700);
		leftMotor.setAcceleration(700);
		rightMotor.setSpeed(360);
	    leftMotor.setSpeed(360);
	    leftMotor.backward();
	    rightMotor.backward();
	    sleep(1000);
	    if(Math.random() * 2 < 1) {
		    rightMotor.forward();
		    leftMotor.backward();
	    } else {
	    	rightMotor.backward();
		    leftMotor.forward();
	    }
//	    Thread.sleep((long) (Math.random() * 1500) + 500);
	    sleep((long) (Math.random() * 1500) + 500);
	    stopMotor();
	}
	
	public static void stopMotor() throws RemoteException {
		rightMotor.stop(true);
	    leftMotor.stop(true); 
	}
	
	public static void runAway(FearSensor fear) throws RemoteException, InterruptedException {
		turnAround();
		setMotor(900 - (int)fear.getFearPercent() * 900, 900 - (int) fear.getFearPercent() * 900);
//		Thread.sleep(2000 - (long) (2000 * fear.getFearPercent()));
		sleep((int) (2000 * fear.getFearPercent()));
		stopMotor();
	}
	
	public static void sleep(int milliseconds) {
		long time = (long) (System.currentTimeMillis() + milliseconds);
		boolean exitSleep = false;
		while (!exitSleep && System.currentTimeMillis() < time) {
			// Update exitSleep value with sensor values.
			//exitSleep = fearSensor.isStartled();
		}
	}
	
	public void run() {
		while(true && running) {
			fearSensor.read();
//			System.out.println("Updating sensors...");

			BRICKS[1].getTextLCD().drawString("" + fearSensor.read(),  0, 4);
			BRICKS[1].getTextLCD().drawString("fear " + fearSensor.isStartled(),  0, 6);
		}
	}
	
	

}
