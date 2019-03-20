/**
 * This class is the main class used for Lab5
 * 
 * @author Alex Lo,  260712192 
 * @author Aymar Muhikira,  260860188
 * @author Haoran Du
 * @author Charles Liu
 * @author David Schrier
 * @author Nicki Hu
 * 
 */
package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.project.*;
import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;



public class Project {

	//get port for US sensor
	private static final Port usPort = LocalEV3.get().getPort("S1"); 		
	private static final EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(usPort);
	// Motor Objects, and Robot related parameters
	private static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RADIUS = 2.05;
	public static final double WHEEL_BASE = 9.5;
	public static final double TILE_SIZE = 30.48;
	public static final int ROTATE_SPEED = 100;
	public static final int FORWARD_SPEED = 50;
	// Parameters related to the map and color detection
	public static int LLx = 2;
	public static int LLy = 2;
	public static int URx = 5;
	public static int URy = 5;
	public static int TN_LLx = 2;
	public static int TN_LLy = 2;
	public static int TN_URx = 2;
	public static int TN_URy = 2;
	public static int SZ_LLx = 2;
	public static int SZ_LLy = 2;
	public static int SZ_URx = 5;
	public static int SZ_URy = 5;
	public static int corner = 5;
	public static ColorDetect colorDetection = new ColorDetect();
	public static int TR = 3;
	public static EV3GyroSensor   gyrosensor;
	public static SampleProvider  gyro_sp;
	public static float [] gyro_sample;
	private static final Port gyroPort = LocalEV3.get().getPort("S4");
	private static final Port lightPort = LocalEV3.get().getPort("S3");
	public static EV3ColorSensor lightSensor = new EV3ColorSensor(lightPort);

	/**
	 * This is the main method that runs on the EV3 
	 * @param args
	 * @return Not used
	 * @throws OdometerExceptions 
	 */

	public static void main(String[] args) throws OdometerExceptions{

		int buttonChoice;
		// Initialize objects used for odometry
		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, WHEEL_BASE, WHEEL_RADIUS); 
		OdometryCorrection odometryCorrection = new OdometryCorrection();
		gyrosensor = new EV3GyroSensor(gyroPort);
	    gyro_sp = gyrosensor.getAngleAndRateMode();
	    gyro_sample = new float[gyro_sp.sampleSize()];
	    gyrosensor.reset();
		do {
			// clear the display
			lcd.clear();
			// ask the user to start the demo by selecting either left or right
			lcd.drawString("     Start      ", 0, 0);
			lcd.drawString("                ", 0, 1);
			lcd.drawString("                ", 0, 2);
			lcd.drawString("                ", 0, 3);
			lcd.drawString("                ", 0, 4);
			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);//Do the demo once the user chooses to
		lcd.clear();
		//TODO Wifi wifi = new Wifi();
		Thread odoThread = new Thread(odometer);
		odoThread.start();
	    /*USLocalizer USLocalizer = new USLocalizer(odometer, leftMotor, rightMotor, usSensor);
	    Navigation simpleNavigation = new Navigation (odometer, leftMotor, rightMotor, WHEEL_RAD);
		LightLocalizer2 lightLocatizer = new LightLocalizer2(odometer, lightSensor,leftMotor, rightMotor, simpleNavigation);
		USLocalizer.fallingEdge();
		lightLocatizer.doLocalization();*/
		gyrosensor.reset();
		MapDriver map_drive = new MapDriver(odometer, leftMotor, rightMotor);
		map_drive.drive();
		/*Thread odoCorrectionThread = new Thread(odometryCorrection);
	    odoCorrectionThread.start();*/
	    /*simpleNavigation.travelTo(3*TILE_SIZE, 3*TILE_SIZE);
	    gyro_sp.fetchSample(gyro_sample, 0);
	    angle = gyro_sample[0];
		if(angle < 0) {
			angle += 360;
		}
		angle = 360 - angle;
	    System.out.println("actual angle: "+angle);
	    System.out.println("odometry angle:"+odometer.getXYT()[0]);
	    simpleNavigation.travelTo(3*TILE_SIZE, 0);
	    gyro_sp.fetchSample(gyro_sample, 0);
	    angle = gyro_sample[0];
		if(angle < 0) {
			angle += 360;
		}
		angle = 360 - angle;
	    System.out.println("actual angle: "+angle);
	    System.out.println("odometry angle:"+odometer.getXYT()[0]);
	    simpleNavigation.travelTo(0, 0);
	    gyro_sp.fetchSample(gyro_sample, 0);
	    angle = gyro_sample[0];
		if(angle < 0) {
			angle += 360;
		}
		angle = 360 - angle;
	    System.out.println("actual angle: "+angle);
	    System.out.println("odometry angle:"+odometer.getXYT()[0]);*/
		//Do the localization automatically (without user interaction)
		/*USLocalizer USLocalizer = new USLocalizer(odometer, leftMotor, rightMotor, usSensor);
		LightLocalizer lightLocatizer = new LightLocalizer(odometer, leftMotor, rightMotor);
		SimpleNavigation simpleNavigation = new SimpleNavigation (leftMotor, rightMotor);
		USLocalizer.fallingEdge();
		lightLocatizer.localize();*/
		//Navigate through the map
		//simpleNavigation.doNavigation(LLx, LLy, URx, URy);
		//Sound.beep();//Beep once the demo is complete		
		//Stop the program if the user presses another button
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}