	public void objectApproach(){
		//if current reading is within 3 feet and farther than 2 inches
		if ((currentDistance) <= DETECT_OBJECT_FAR_THRESHOLD &&
			currentDistance > DETECT_OBJECT_NEAR_THRESHOLD){

			float speedRatio = (currentDistance) / DETECT_OBJECT_FAR_THRESHOLD;
			
			LCD.drawString("Speed Ratio", 0, 0);
			LCD.drawInt(speedRatio, 6, 0, 1);
			
			float currentSpeed = speedRatio * DETECT_OBJECT_MAX_SPEED;
			
			Motor.B.setSpeed(currentSpeed);
			Motor.C.setSpeed(currentSpeed);
			Motor.B.forward();
			Motor.C.forward();

			} else if (currentDistance <= DETECT_OBJECT_NEAR_THRESHOLD) {
			LCD.drawString("Stop and backup", 0, 0);
			Motor.B.stop();
			Motor.C.stop();
		}
	}
	
	void leaveObjectMethod() {
		//backup
		Motor.B.setSpeed(-400);
		Motor.C.setSpeed(-400);
		Motor.B.forward();
		Motor.C.forward();
		Thread.sleep (2000);
		//turn around
		if (Math.random() % 2 == 1) { // turn right
			Motor.B.setSpeed(-400);
			Motor.C.setSpeed(-400);
			Motor.B.forward();
			Motor.C.forward();
			sleep(//RANDOM);
			} else { // turn left
				Motor.B.setSpeed(-400);
				Motor.C.setSpeed(-400);
				Motor.B.forward();
				Motor.C.forward();
				sleep(//RANDOM);
			}
			//pause
			Motor.B.setSpeed(0);
			Motor.C.setSpeed(0);
			Thread.sleep(2000);
	}
