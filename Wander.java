import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.LED;
import lejos.hardware.motor.Motor;
import lejos.remote.ev3.RemoteEV3;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

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

	public static void remoteLEDTest()
    {
        String[] names = {"g8ev3", "EV3"};
        Brick[] bricks = new Brick[names.length];
        try {
            bricks[0] = BrickFinder.getLocal();
            for(int i = 1; i < bricks.length; i++)
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
		
		remoteLEDTest();
		
		
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

}
