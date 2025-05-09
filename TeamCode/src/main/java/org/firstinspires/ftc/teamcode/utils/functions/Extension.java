package org.firstinspires.ftc.teamcode.utils.functions;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.utils.runnables.SlidesRunnable;

public class Extension {
    private boolean isExtended = false, isInitialized = false;
    private SlidesRunnable extensionPID;
    private Thread intakeThread;
    private double targetPosition;
    public boolean initialize(HardwareMap hardwareMap){
        if (!isInitialized){
            Logger.info("Init started");
            isInitialized = true;
            extensionPID = new SlidesRunnable(hardwareMap, "EM", 0, 0.01, 0.00003, 0.0008, true, 0.8);
            intakeThread = new Thread(extensionPID);
            intakeThread.start();
            extensionPID.setMaxPower(0.7);
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
            extensionPID.setTarget(200);
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
            extensionPID.setTarget(40);
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
    public boolean isExtended() {
        return isExtended;
    }
    public double getTargetPosition(){
        return extensionPID.getTargetPosiion();
    }
    public void stopIntakeThread() {
        Logger.info("Terminating intake thread...");
        if (intakeThread != null) {
            intakeThread.interrupt();
            intakeThread = null;
            Logger.info("Successfully terminated intakeThread");
        } else {
            Logger.warn("intakeThread is null, aborting termination");
        }
    }
    public double getSlidePosition(){ return extensionPID.getCurrentPosition(); }
    public double getSlidePower(){ return extensionPID.getPower(); }
    public DcMotor.RunMode getSlideMode(){ return extensionPID.getMode(); }
}