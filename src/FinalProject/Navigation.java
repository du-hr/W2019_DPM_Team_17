/**
 * This class is used to navigate from the robot's current position to a specified waypoint
 */
package FinalProject;

import Odometer.Odometer;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import static FinalProject.Main.*;

public class Navigation {
  private static Odometer odometer;
  private static EV3LargeRegulatedMotor leftMotor;
  private static EV3LargeRegulatedMotor rightMotor;
  private static boolean isNavigating = false;
  private static SampleProvider gyroSensor;
  private static float[] gyroData;

  /**
   * This is the constructor for the class 
   * @param odometer   The odometer
   * @param leftMotor  The left motor of the robot
   * @param rightMotor The right motor of the robot
   * @param gyroSensor  The gyrosensor
   * @param gyroData    The array to store angle readings from the gyrosensor
   * @return Not used
   */
  public Navigation(Odometer odometer, EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor, SampleProvider gyroSensor, float[] gyroData) {
    Navigation.odometer = odometer;
    Navigation.leftMotor = leftMotor;
    Navigation.rightMotor = rightMotor;
    Navigation.gyroSensor = gyroSensor;
    Navigation.gyroData = gyroData;
  }

  /**
   * This method is used to travel from the current position to specified coordinates x and y
   * Corresponding to coordinates on the map (without the tile size)
   * @return Not used
   */
  public static void travelTo(double x, double y) {

    x = x * TILE_SIZE;
    y = y * TILE_SIZE;


    // reset motors
    leftMotor.stop();
    rightMotor.stop();

    leftMotor.setAcceleration(3000);
    rightMotor.setAcceleration(3000);


    isNavigating = true;

    // calculate trajectory path and angle
    double[] odoData = odometer.getXYT();
    double X = odoData[0];
    double Y = odoData[1];
    double trajectoryX = x - X;
    double trajectoryY = y - Y;
    double trajectoryAngle = Math.toDegrees(Math.atan2(trajectoryX, trajectoryY));

    // rotate to correct angle
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);
    turnTo(trajectoryAngle);

    double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);

    // move forward correct distance
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
    leftMotor.rotate(convertDistanceForMotor(trajectoryLine), true);
    rightMotor.rotate(convertDistanceForMotor(trajectoryLine), false);
  }

  // to make sure the angle of each turn is the minimum angle possible
  /**
   * This method is used find which way we should turn (clockwise or anticlockwise)
   * and by how much degrees
   * @param heading  the angle to turn to
   * @return Not used
   */
  public static void turnTo(double heading) {
	  double[] odoData = odometer.getXYT();
		double theta = getGyroData();
		//Replace previous line of code with previous comment to test correction
		//Also change turnRight and turnLeft with turnRight2 and turnLeft2
		double angle = heading-theta;
		if(angle < -180.0) {
			angle = angle + 360;
			turnRight(angle);
		} 
		else if (angle > 180.0) {
			turnLeft(360 - angle);
		} 
		else if (angle < 0) {
			turnLeft(Math.abs(angle));
		}
		else if(angle > 0) {
			turnRight(angle);
		}
  }

  /**
   * This method is used to correct the angle in the odometer based on the
   * reading of the gyrosensor (used as a safety measure to get an accurate angle) 
   * @return Not used
   */
  public static void angleCorrection() {
    gyroSensor.fetchSample(gyroData, 0);
    if (gyroData[0] >= 0) {
      odometer.setXYT(odometer.getXYT()[0], odometer.getXYT()[1], gyroData[0]);
    } else {
      odometer.setXYT(odometer.getXYT()[0], odometer.getXYT()[1], 360 + gyroData[0]);
    }
  }

  /**
   * This method is used to turn counterclockwise by a certain angle, relying
   * on the gyrosensor reading to be as close to the desired angle as possible
   * @param degree  the angle by how much to turn
   * @return Not used
   */
  public static void turnLeft(double degree) {
    if (degree <= 1) {
      return;
    }
    int speed;
    double minAngle = 0;
    double angle = getGyroData();
    double angle1 = getGyroData();
    while ((Math.abs(angle - angle1 - degree) >= 1)
        && (Math.abs((angle1 - angle) - (360 - degree)) >= 1)) {
      minAngle = Math.min((Math.abs(angle - angle1 - degree)),
          Math.abs((angle1 - angle) - (360 - degree)));
      speed = (int) (80 - 25 / (minAngle + 1));
      leftMotor.setSpeed(speed);
      rightMotor.setSpeed(speed);
      leftMotor.backward();
      rightMotor.forward();
      angle1 = getGyroData();
    }
    leftMotor.stop(true);
    rightMotor.stop();
  }

  /**
   * This method is used to turn clockwise by a certain angle, relying
   * on the gyrosensor reading to be as close to the desired angle as possible
   * @param degree  the amount of degrees by which to turn
   * @return Not used
   */
  public static void turnRight(double degree) {
    if (degree <= 1) {
      return;
    }
    double minAngle = 0;
    int speed;
    double angle = getGyroData();
    double angle1 = getGyroData();
    while ((Math.abs(angle1 - angle - degree) >= 1)
        && (Math.abs((angle - angle1) - (360 - degree)) >= 1)) {
      minAngle = Math.min((Math.abs(angle1 - angle - degree)),
          Math.abs((angle - angle1) - (360 - degree)));
      speed = (int) (80 - 25 / (minAngle + 1));
      leftMotor.setSpeed(speed);
      rightMotor.setSpeed(speed);
      leftMotor.forward();
      rightMotor.backward();
      angle1 = getGyroData();
    }
    leftMotor.stop(true);
    rightMotor.stop();
  }


  /*
   * returns: whether or not the vehicle is currently navigating
   */
  public boolean isNavigating() {
    return isNavigating;
  }

  /**
   * This method allows the conversion of a distance to the total rotation of each wheel need to
   * cover that distance.
   * 
   * @param radius
   * @param distance
   * @return
   */
  public static int convertDistanceForMotor(double distance) {
    return (int) (360 * distance / (2 * Math.PI * WHEEL_RADIUS));
  }

  /**
   * This method is called to get the angle from the gyrosensor 
   * @return Not used
   */
  public static double getGyroData() {
    gyroSensor.fetchSample(gyroData, 0);
    // we correct the angle in odometer and return it here as the
    // reading of heading angle from gyro sensor
    angleCorrection();
    return odometer.getXYT()[2];
  }


}
