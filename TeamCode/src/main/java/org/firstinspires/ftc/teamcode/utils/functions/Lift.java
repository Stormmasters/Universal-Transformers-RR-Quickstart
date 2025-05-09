package org.firstinspires.ftc.teamcode.utils.functions;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.teamcode.utils.runnables.SlidesRunnable;

public class Lift {
    private boolean isExtended = false, isInitialized = false;
    private Thread slideThread, armThread;
    private SlidesRunnable liftPID;
    private double armRetracted = 0, armExtended = 1;
    ServoImplEx arm;
    public boolean isExtended(){
        return isExtended;
    }
    public void resetSlides(){
        liftPID.resetSlides();
    }
    public boolean initialize(HardwareMap hardwareMap){
        if (!isInitialized){
            Logger.info("Init started");
            //arm = hardwareMap.get(ServoImplEx.class, "arm");
            isInitialized = true;
            liftPID = new SlidesRunnable(hardwareMap, "LM", 0, 0.06, 0.0003, 0.001, false, 0.8);
            slideThread = new Thread(liftPID, "SlidePIDThread");
            slideThread.start();
            Logger.info("Successfully initialized");
            return true;
        }
        else {
            Logger.error("Failed to init; Already initialized");
            return false;
        }
    }
    public boolean extend() {
        if (isInitialized && !isExtended){
            Logger.info("Extending slides...");
            liftPID.setTarget(500);
            //arm.setPosition(armExtended);
            isExtended = true;
            return true;
        }
        else if (!isInitialized){
            Logger.error("Failed to extend slides; Slides not initialized");
            return false;
        }
        else {
            Logger.error("Failed to extend slides; already extended");
            return false;
        }
    }
    public boolean retract() {
        if (isInitialized && isExtended){
            Logger.info("Retracting slides...");
            liftPID.setTarget(0);
            //arm.setPosition(armRetracted);
            isExtended = false;
            return true;
        }
        else if (!isInitialized){
            Logger.error("Failed to retract slides; Slides not initialized");
            return false;
        }
        else {
            Logger.error("Failed to retract slides; already retracted");
            return false;
        }
    }
    public void stopSlideThreads(){
        Logger.info("Terminating slide threads...");
        if (slideThread != null) {
            slideThread.interrupt();
            slideThread = null;
            Logger.info("Successfully terminated slideThread");
        }
        else {
            Logger.warn("slideThread is null, aborting termination");
        }
    }
    public double getSlidePosition(){ return liftPID.getCurrentPosition(); }
    public double getSlidePower(){ return liftPID.getPower(); }
    public DcMotor.RunMode getSlideMode(){ return liftPID.getMode(); }
}