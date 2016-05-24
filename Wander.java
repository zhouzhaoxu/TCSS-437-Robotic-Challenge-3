import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.LED;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;
import lejos.utility.Delay;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class Wander {
	
	static RegulatedMotor cannon = Motor.A;
	static RegulatedMotor leftMotor = Motor.B;
    static RegulatedMotor rightMotor = Motor.C;
    

    //The default speed of the robot.
    public final static int DEFAULT_SPEED = 300;
    
	/*
	 * Wander Constants
	 */	
	//If set to 50, then it has even odds of veering right or left.
	public final static int VEER_RIGHT_CHANCE  = 50;
	//How much to offset the odds of veering right upon each time we veer right without 
	//veering left.
	public final static int DISTRO_OFFSET = 10;
	//How much faster a motor should go if the car is veering in that direction while wandering.
	public final static int VEER_SPEED_OFFSET = 150;
	//The minimum amount of time that the robot will wander before it chooses a new direction.
	public final static int MIN_WANDER_TIME = 1000;
	//The maximum amount of time that the robot will wander before it chooses a new direction.
	public final static int MAX_WANDER_TIME = 2000;
	
	public final static String[] names = {"g8ev3", "EV3"};
	public final static Brick[] bricks = new Brick[names.length];
	

	public static void remoteLEDTest()
    {
        //String[] names = {"g8ev3", "EV3"};
        //Brick[] bricks = new Brick[names.length];
        try {
//            bricks[0] = BrickFinder.getLocal();
            for(int i = 0; i < bricks.length; i++)
            {
                System.out.println("Connect " + names[i]);
                bricks[i] = new RemoteEV3(BrickFinder.find(names[i])[0].getIPAddress());
            }
            LED[] leds = new LED[bricks.length];
            for(int i = 0; i < bricks.length; i++)
                leds[i] = bricks[i].getLED();
            int i = 0;
            int pat = 1;
            while(Button.ENTER.isUp())
            {
            	bricks[0].getTextLCD().drawString("primary brick",  0, 4);
                bricks[1].getTextLCD().drawString("secondary brick",  0, 7);
                //bricks[2].getTextLCD().drawString("one brick",  0, 7);
                
                leds[(i++) % leds.length].setPattern(0);
                if (i % leds.length == 0)
                {
                    pat = ((pat + 1) % 3) + 1;
                }
                leds[(i) % leds.length].setPattern(pat);
                Delay.msDelay(100);
            }
            for(LED l : leds)
                l.setPattern(0);
        }
        catch (Exception e)
        {
            System.out.println("Got exception " + e);
        }
    }    
	
	public static void main(String[] args) {
		//left or right while wandering.
		int directionDistro = 0;
		int nextWanderTime = 0;
		// TODO Auto-generated method stub
		//leftMotor.setSpeed(850);
		//cannon.setSpeed(900);
		
		//LED TEST
		//remoteLEDTest();
		
		//Brick brick = BrickFinder.getLocal(); // Get the brick
		
		
		for(int i = 0; i < bricks.length; i++)
        {
            System.out.println("Connected to" + names[i]);
            try {
				bricks[i] = new RemoteEV3(BrickFinder.find(names[i])[0].getIPAddress());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		//get the port for color sensor
		Port s1 = bricks[0].getPort("S2"); // Get the sensor port
		// Set the type of sensor to the port.
		EV3ColorSensor colorSensor  = new EV3ColorSensor(s1); 		
		colorSensor.setCurrentMode("Ambient"); // set the sensor mode
		FearSensor fearSensor = new FearSensor(colorSensor); // Create a sensor filter.
		long time = System.currentTimeMillis();
		bricks[1].getTextLCD().clear();
		while(System.currentTimeMillis() - time < 45000) {
			//System.out.println("" + fearSensor.read());
			
			bricks[1].getTextLCD().drawString("" + fearSensor.read(),  0, 4);
			if(fearSensor.isStartled()) {
				remoteMotorTest();
				bricks[1].getTextLCD().drawString("fear true",  0, 7);
			}
		}
//		
		colorSensor.close();
		System.out.println("End of program Main");
		

//        for(int i = 1; i < bricks.length; i++)
//            ((RemoteRequestEV3) bricks[i]).disConnect();
//		while (fearSensor.getFearPercent() > 0.0) {
//			fearSensor.read();
//			if (fearSensor.isStartled()) {
//				// Run away
//				//..
//				//..
//				//..
//				//..
//				// Reset startled flag
//				fearSensor.resetStartled();
//			}
//		}
		
		
//		long startTime = System.currentTimeMillis();
//		while(true) {
//			if ((System.currentTimeMillis() - startTime) > nextWanderTime) {
//				startTime = System.currentTimeMillis();
//				//No obstacle or line detected. Wander.
//				directionDistro = randomBiasedWalk(directionDistro);
//				nextWanderTime = (int) ((Math.random() * (MAX_WANDER_TIME - MIN_WANDER_TIME)) 
//						+ MIN_WANDER_TIME);
//		    }
//		}
	}
	

	public static int randomBiasedWalk(int directionDistro) {
		/*
		 * Starting with 50:50 odds, randomly determine if the robot will veer right or left
		 * for a random amount of time.
		 * If it veers right, the odds become 60:40 in left's favor. If it veers right again,
		 * the odds become 70:30. If it then veers left, the odds become 60:40, and so on.
		 */
		 if ((int) Math.random() * 100 > (VEER_RIGHT_CHANCE + directionDistro * DISTRO_OFFSET)) {
			 setSpeed(DEFAULT_SPEED + VEER_SPEED_OFFSET, DEFAULT_SPEED);
			 return ++directionDistro;
		 } else {
			 setSpeed(DEFAULT_SPEED, DEFAULT_SPEED + VEER_SPEED_OFFSET);	
			 return --directionDistro;
		 }
	}
	
	public static void setSpeed(int velocity_left, int velocity_right) {
		leftMotor.setSpeed(velocity_left);
		rightMotor.setSpeed(velocity_right);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	public static void remoteMotorTest() {
	    try {
	        
//	        RegulatedMotor[] motors = new RegulatedMotor[2];
	        //get the port for motor
//	        motors[0] = new EV3LargeRegulatedMotor(bricks[0].getPort("B"));
//	        motors[1] = new EV3LargeRegulatedMotor(bricks[0].getPort("C"));
	        final RMIRegulatedMotor   rightMotor= ((RemoteEV3) bricks[1]).createRegulatedMotor("B", 'L');
	        final RMIRegulatedMotor   leftMotor=((RemoteEV3) bricks[1]).createRegulatedMotor("C", 'L');
//	        for(int i = 1; i < bricks.length; i++)
//	        {
//	            motors[0] = (RegulatedMotor) ( bricks[0]).createRegulatedMotor("B", 'L');
//	            motors[1] = (RegulatedMotor) ( bricks[0]).createRegulatedMotor("C", 'L');
//	        }
	 
//	        for(RegulatedMotor m : motors)
//	        {
//	            m.setAcceleration(900);
//	            m.setSpeed(600);
//	        }
	        rightMotor.setAcceleration(900);
	        rightMotor.setSpeed(600);
	        rightMotor.forward();
	        leftMotor.setAcceleration(900);
	        leftMotor.setSpeed(600);
	        leftMotor.forward();
	        Thread.sleep(3000);
	        // rightMotor.wait(timeout);
	        rightMotor.close();
	        leftMotor.close();
	        System.out.println("End of program Motor");
	        Thread.sleep(3000);
//	 
//	        for(RegulatedMotor m : motors)
//	            m.rotate(720);
//	        for(RegulatedMotor m : motors)
//	            m.rotate(-720);
//	        for(RegulatedMotor m : motors)
//	            m.rotate(720, true);
//	        for(RegulatedMotor m : motors)
//	            m.waitComplete();
//	 
//	        for(RegulatedMotor m : motors)
//	            m.rotate(-720, true);
//	        for(RegulatedMotor m : motors)
//	            m.waitComplete();
//	 
//	        for(int i = 0; i < 4; i++)
//	            for(RegulatedMotor m : motors)
//	                m.rotate(90);
//	        for(RegulatedMotor m : motors)
//	            m.rotate(-720, true);
//	        for(RegulatedMotor m : motors)
//	            m.waitComplete();
//	 
//	        for(int i = 0; i < motors.length; i++)
//	        {
//	            motors[i].setSpeed((i+1)*100);
//	            motors[i].setAcceleration((i+1)*100);
//	        }
//	        for(int i = 0; i < motors.length; i++)
//	            motors[i].rotate((i+1)*360, true);
//	        for(RegulatedMotor m : motors)
//	            m.waitComplete();
	 
//	        for(RegulatedMotor m : motors)
//	            m.close();
	 
	    }
	    catch (Exception e)
	    {
	    	try {
	    		rightMotor.close();
	    		leftMotor.close();
				System.out.println("Got exception 1 " + e);
				Thread.sleep(5000);
				
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				rightMotor.close();
				leftMotor.close();
				e1.printStackTrace();
				
			}
	    	rightMotor.close();
	    	leftMotor.close();
	    	System.out.println("End outter catch");
	    }
	}    

}
