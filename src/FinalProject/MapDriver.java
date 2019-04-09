package FinalProject;


import static FinalProject.Navigation.*;
import java.util.concurrent.TimeUnit;
import Odometer.Odometer;
import lejos.hardware.Sound;
import static FinalProject.Main.*;
import static FinalProject.CanScanner.*;


public class MapDriver{
  private static int counter;
  private static int counter_1;
  private static int counter_2;
  private static int counter_3;
  private static int counter_4;
  private static double islandX;
  private static double islandY;
  private static double bridgeX;
  private static double bridgeY;
  
  private Odometer odometer;
  //default constructor
  public MapDriver(Odometer odometer) {
    this.odometer = odometer;
  }
  
  public void drive() throws InterruptedException{
    // repeat for 5 times
    for (int i=0; i<5; i++) {
    moveToBridge();
    travelThroughBridge();
    moveToSearchZone();
    searchCan();
    }
  }

  public void moveToBridge() {
    ClawMovement.holdCan();
    if (Math.abs(TN_URy-TN_LLy) < Math.abs(TN_URx-TN_LLx)){
      bridgeY = TN_LLy + 0.5;
      bridgeX = TN_LLx;
    }
    else {
      bridgeX = TN_LLx + 0.5;
      bridgeY = TN_LLy;
    }
    travelTo(bridgeX-0.5,bridgeY);
    travelTo(bridgeX,bridgeY);
  }


  public void travelThroughBridge() {
    if (Math.abs(TN_URy-TN_LLy) < Math.abs(TN_URx-TN_LLx)){
      islandY = TN_LLy + 0.5;
      islandX = TN_LLx + Math.abs(TN_URx-TN_LLx) + 0.5;
    }
    else {
      islandX = TN_LLx + 0.5;
      islandY = TN_LLy + Math.abs(TN_URy-TN_LLy) + 0.5;
    }
    travelTo(islandX,islandY);
    ClawMovement.releaseCan();
  }

  public void moveToSearchZone() {
    travelTo(SZ_LLx,SZ_LLy);
    turnTo(0);
  }
  
  public void searchCan() throws InterruptedException{
    int SZ_length = SZ_URy - SZ_LLy;
    int SZ_width = SZ_URx - SZ_LLx;
    
    // search on line 1
    if(counter < SZ_length) {
      // start from the previous scanning position
      travelTo(SZ_LLx,SZ_LLy+counter_1);
      // Position the heading of the robot to positive y axis
      turnTo(0);
      // start the CanScanner thread
      CanScanner.initialHeading = 0;
      isScanning = true;
      // let the program sleep for 3 seconds to leave 
      // enough time for scanning
      TimeUnit.SECONDS.sleep(3);
      // detected a can while scanning
      if (isScanning == false) {
        moveToCan(CanScanner.degreesOfTurning,CanScanner.detectedCanDistance);
        doColorDetection();
        doWeightDetection();
        moveCanBack();
        return;
      }
      // did not find a can during the scan
      else {
        // manually stop the CanScanner thread after 3 seconds
        isScanning = false;
        // reset the heading to prepare for the next scanning in the next tile
        turnTo(0);
        counter_1++;
        travelTo(SZ_LLx,SZ_LLy+counter_1);
        counter++;
        if (counter_1 < SZ_length) {
          searchCan(); // recursive method
        }
      }
    }
    
    // search on line 2
    else if (counter >= SZ_length && counter < SZ_length + SZ_width) {
      travelTo(SZ_LLx,SZ_URy);
      // start from the previous scanning position
      travelTo(SZ_LLx+counter_2,SZ_URy);
      // Position the heading of the robot to positive x axis
      turnTo(90);
      CanScanner.initialHeading = 90;
      // start the CanScanner thread
      isScanning = true;
      // let the program sleep for 3 seconds to leave 
      // enough time for scanning
      TimeUnit.SECONDS.sleep(3);
      // detected a can while scanning
      if (isScanning == false) {
        moveToCan(CanScanner.degreesOfTurning,CanScanner.detectedCanDistance);
        doColorDetection();
        doWeightDetection();
        moveCanBack();
        return;
      }
      // did not find a can during the scan
      else {
        // manually stop the CanScanner thread after 3 seconds
        isScanning = false;
        // reset the heading to prepare for the next scanning in the next tile
        turnTo(90);
        counter_2++;
        travelTo(SZ_LLx+counter_2,SZ_URy);
        counter++;
        if (counter_2 < SZ_width) {
          searchCan(); // recursive method
        }
      }
    }
    
    //search on line 3
    else if (counter >= SZ_length + SZ_width && counter < 2*SZ_length + SZ_width) {
      travelTo(SZ_LLx,SZ_URy);
      travelTo(SZ_URx,SZ_URy);
      // start from the previous scanning position
      travelTo(SZ_URx,SZ_URy-counter_3);
      // Position the heading of the robot to negative y axis
      turnTo(180);
      // start the CanScanner thread
      CanScanner.initialHeading = 180;
      isScanning = true;
      // let the program sleep for 3 seconds to leave 
      // enough time for scanning
      TimeUnit.SECONDS.sleep(3);
      // detected a can while scanning
      if (isScanning == false) {
        moveToCan(CanScanner.degreesOfTurning,CanScanner.detectedCanDistance);
        doColorDetection();
        doWeightDetection();
        moveCanBack();
        return;
      }
      // did not find a can during the scan
      else {
        // manually stop the CanScanner thread after 3 seconds
        isScanning = false;
        // reset the heading to prepare for the next scanning in the next tile
        turnTo(180);
        counter_3++;
        travelTo(SZ_URx,SZ_URy-counter_3);
        counter++;
        if (counter_3 < SZ_length) {
          searchCan(); // recursive method
        }
      }
    }
    
    // search on line 4
    else if (counter >= 2*SZ_length + SZ_width && counter <  2*SZ_length + 2*SZ_width) {
      travelTo(SZ_URx,SZ_LLy);
      // start from the previous scanning position
      travelTo(SZ_URx-counter_4,SZ_LLy);
      // Position the heading of the robot to negative x axis
      turnTo(-90);
      // start the CanScanner thread
      CanScanner.initialHeading = -90;
      isScanning = true;
      // let the program sleep for 3 seconds to leave 
      // enough time for scanning
      TimeUnit.SECONDS.sleep(3);
      // detected a can while scanning
      if (isScanning == false) {
        moveToCan(CanScanner.degreesOfTurning,CanScanner.detectedCanDistance);
        doColorDetection();
        doWeightDetection();
        moveCanBack();
        return;
      }
      // did not find a can during the scan
      else {
        // manually stop the CanScanner thread after 3 seconds
        isScanning = false;
        // reset the heading to prepare for the next scanning in the next tile
        turnTo(-90);
        counter_4++;
        travelTo(SZ_URx-counter_4,SZ_LLy);
        counter++;
        if (counter_4 < SZ_width) {
          searchCan(); // recursive method
        }
      }
    }
  }
  

  private void moveToCan(double detectedCanHeading, double detectedCanDistance) {
    detectedCanDistance = detectedCanDistance / (double)180 * Math.PI; // CONVERT TO RADIAN
    double[] odoData = odometer.getXYT();
    double scanningPositionX = odoData[0];
    double scanningPositionY = odoData[1];
    double canPositionX;
    double canPositionY;
    
    
    if (CanScanner.initialHeading == 0) {
      canPositionX = scanningPositionX + detectedCanDistance * Math.sin(detectedCanHeading);
      canPositionY = scanningPositionY + detectedCanDistance * Math.cos(detectedCanHeading);
      travelTo(canPositionX,canPositionY);
      travelTo(scanningPositionX,scanningPositionY);
      turnTo(0);
    }
    if (CanScanner.initialHeading == 90) {
      canPositionX = scanningPositionX + detectedCanDistance * Math.cos(detectedCanHeading);
      canPositionY = scanningPositionY - detectedCanDistance * Math.sin(detectedCanHeading);
      travelTo(canPositionX,canPositionY);
      travelTo(scanningPositionX,scanningPositionY);
      turnTo(90);
    }
    if (CanScanner.initialHeading == 180) {
      canPositionX = scanningPositionX - detectedCanDistance * Math.sin(detectedCanHeading);
      canPositionY = scanningPositionY - detectedCanDistance * Math.cos(detectedCanHeading);
      travelTo(canPositionX,canPositionY);
      travelTo(scanningPositionX,scanningPositionY);
      turnTo(180);
    }
    if (CanScanner.initialHeading == -90) {
      canPositionX = scanningPositionX - detectedCanDistance * Math.cos(detectedCanHeading);
      canPositionY = scanningPositionY + detectedCanDistance * Math.sin(detectedCanHeading);
      travelTo(canPositionX,canPositionY);
      travelTo(scanningPositionX,scanningPositionY);
      turnTo(-90);
    }
  }

  private void doColorDetection() {
    CanColorDetection.detectCanColor();
  }
  
  private void doWeightDetection() {
    CanWeightDetection.detectCanWeight();
  }
  
  public void moveCanBack() {
    if (CanScanner.initialHeading == 0) {
      travelTo(SZ_LLx,SZ_LLy);
      goHome();
    }
    if (CanScanner.initialHeading == 90) {
      travelTo(SZ_LLx,SZ_URy);
      travelTo(SZ_LLx,SZ_LLy);
      goHome();
    }
    if (CanScanner.initialHeading == 180) {
      travelTo(SZ_URx,SZ_URy);
      travelTo(SZ_LLx,SZ_URy);
      travelTo(SZ_LLx,SZ_LLy);
      goHome();
    }
    if (CanScanner.initialHeading == -90) {
      travelTo(SZ_URx,SZ_LLy);
      travelTo(SZ_LLx,SZ_LLy);
      goHome();
    }
  }

  private void goHome() {
    travelTo(islandX,islandY);
    travelTo(bridgeX,bridgeY);
    travelTo(Homex,Homey);
    ClawMovement.releaseCan();
    travelTo(Homex, Homey);
    Sound.twoBeeps();
    Sound.twoBeeps();
    Sound.beep();
  }
}
