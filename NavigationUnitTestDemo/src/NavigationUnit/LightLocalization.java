package NavigationUnit;

import Odometer.*;

import static NavigationUnit.Main.*;
import static NavigationUnit.Navigation.*;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class LightLocalization {
	//Parameters used to know the location (odometry) and navigate
	public static int ROTATION_SPEED = 100;
	private double SENSOR_DIST = 2;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	public Navigation navigation;
	//Parameters related to the light sensor (input)
	private static Port lightPort = LocalEV3.get().getPort("S4");
	private SensorModes lightSensor;
	private SampleProvider color;
	private float[] colorData;
	private float prevColor = 0;
	private int numLines = 0;
	private double[] lineAngle = new double[4];

	public LightLocalization(Odometer odometer, EV3LargeRegulatedMotor leftMotor,
			EV3LargeRegulatedMotor rightMotor) {
		// get incoming values for variables
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor; //We create a navigation class
		this.lightSensor = new EV3ColorSensor(lightPort);
		this.color = lightSensor.getMode("Red");
		this.colorData = new float[lightSensor.sampleSize()];
	}
	//Uncomment if needed, I'm not sure what it is supposed to do
	/*public static LightLocalization lightLocalizer(Odometer odometer, SampleProvider colorSensor, float[] colorData,
      EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {

    LightLocalization lightLocalizer = new LightLocalization(odometer, colorSensor, colorData, leftMotor,rightMotor);
    return lightLocalizer;
}*/

	public void findOrigin() {
		turnTo(Math.PI / 4);
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		color.fetchSample(colorData, 0);
		// use a differential filter to detect lines
		float colordiff = prevColor - colorData[0];
		prevColor = colorData[0];
		while (colorData[0] >= 0.41) {//We move forward until we detect a line
			color.fetchSample(colorData, 0);
			colordiff = prevColor - colorData[0];
			prevColor = colorData[0];
			leftMotor.forward();
			rightMotor.forward();
		}
		Sound.beep();
		//Once a line is detected, we move backward a specific distance
		leftMotor.stop(true);
		rightMotor.stop();
		leftMotor.rotate(convertDistance(WHEEL_RADIUS, -9), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, -9), false);

	}
	
	public void doLightLocalization() {
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		//Start by getting close to the origin
		findOrigin();
		while (numLines < 4) {//Rotate and detect the 4 lines the sensor comes across
			leftMotor.forward();
			rightMotor.backward();
			color.fetchSample(colorData, 0);
			float colordiff = prevColor - colorData[0];
			prevColor = colorData[0];
			if (colordiff >= 0.05) {
				lineAngle[numLines] = odometer.getXYT()[2];//Store the angle for each line
				numLines++;
				Sound.beep();
			}
		}
		leftMotor.stop(true);
		rightMotor.stop();
		double dX, dY, thetax, thetay;//Variables used to calculate the 0� direction and the origin
		//From the 4 angles stored, calculate how off from the origin and 0� the robot is
		thetay = lineAngle[3] - lineAngle[1];
		thetax = lineAngle[2] - lineAngle[0];
		dX = -1 * SENSOR_DIST * Math.cos(Math.toRadians(thetay / 2));
		dY = -1 * SENSOR_DIST * Math.cos(Math.toRadians(thetax / 2));
		odometer.setXYT(dX, dY, odometer.getXYT()[2]-6);//Set the accurate current position
		travelTo(0.0, 0.0);//Navigate to the origin
		leftMotor.setSpeed(ROTATION_SPEED / 2);
		rightMotor.setSpeed(ROTATION_SPEED / 2);
		//Rotate to be in the 0� direction
		if (odometer.getXYT()[2] <= 350 && odometer.getXYT()[2] >= 10.0) {
			leftMotor.rotate(convertAngle(WHEEL_RADIUS, WHEEL_BASE, -odometer.getXYT()[2]), true);
			rightMotor.rotate(-convertAngle(WHEEL_RADIUS, WHEEL_BASE, -odometer.getXYT()[2]), false);
		}
		turnTo(Math.PI/2);
		leftMotor.stop(true);
		rightMotor.stop();
		if(Corner == 0) {
			odometer.setXYT(0.0, 0.0, 0.0);
		}
		else if(Corner == 3) {
			odometer.setXYT(0.0, 7*TILE_SIZE, 0.0);
		}
		else if(Corner == 2) {
			odometer.setXYT(13*TILE_SIZE, 7*TILE_SIZE, 0.0);
		}
		else if(Corner == 1) {
			odometer.setXYT(13*TILE_SIZE,0.0,0.0);
		}
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}