package org.firstinspires.ftc.teamcode.utils.runnables;

import static androidx.core.math.MathUtils.clamp;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.utils.functions.Logger;

public class SlidesRunnable implements Runnable {
    private final DcMotorEx slideMotor;
    private boolean isReversed;
    private double targetPosition;
    private final double kP, kI, kD;
    private double maxPower = 0.8;
    private double error = 0, lastError = 0, integral = 0, derivative = 0;
    private int currentPosition;
    private String identifier;

    public SlidesRunnable(HardwareMap hardwareMap, String identifier, double initialTarget, double kP, double kI, double kD, boolean isReversed, double maxPower) {
        slideMotor = hardwareMap.get(DcMotorEx.class, identifier);
        slideMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        slideMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        slideMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        if (isReversed){
            slideMotor.setDirection(DcMotorEx.Direction.REVERSE);
        }
        this.targetPosition = initialTarget;
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.isReversed = isReversed;
        this.maxPower = maxPower;
    }
    public double getCurrentPosition(){
        return slideMotor.getCurrentPosition();
    }
    public void setMaxPower(double maxPower){
        this.maxPower = maxPower;
    }
    public void setTarget(double newTarget){
        Logger.info("Setting target to " + newTarget);
        this.targetPosition = newTarget;
    }
    public double getTargetPosiion(){
        return targetPosition;
    }
    public DcMotor.RunMode getMode(){ return slideMotor.getMode(); }
    public void resetSlides(){
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // this totally wont cause problems later lol
    }
    public double getPower(){ return slideMotor.getPower(); }
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // PID control loop explanation:
            // There are three different values in PID control loops.
            // There is the error, which is just the difference between
            // the current position and the target position; There's the
            // integral, which is exactly what you would think it would
            // be if you've ever taken a calculus class: it's the sum
            // of the errors over time. Finally, there's the derivative:
            // it's the error minus the previous error. Generally, the
            // the main value is the error. The other values result in
            // uncontrollability when used in excess, and thus are given
            // very small k-values. (k-values being the coefficients of
            // each value)

            // This might all seem a bit counterintuitive. (It is.) The
            // answer to how this works relates to how all of these variables
            // change. Let's say, for example, that the slides start far away
            // from the target. The error, which relates linearly to the
            // distance from the target, immediately increases rapidly. The
            // other values, being not quite as significant, don't increase very
            // much and are given low k-values anyway.

            // Once the motor reaches a hundred or so units from the target
            // position, things start to slow down. (It should be noted that
            // it was always decreasing, but the motorPower only is below the
            // maxPower once it gets really close to the target since the PID
            // values are unaware of the maxPower variable and would try to
            // literally break the sound barrier if they were far enough away
            // from the target.)

            // At this point, the motor begins to slow as a result of a rapidly
            // decreasing error. Once it reaches point 50 (which will prob be
            // given a configurable variable sometime), the integral is given
            // an actual value, but error has decreased by a lot. By point zero,
            // it's mostly stopped and quickly accounts for the error. And that's
            // how it works. --Fraser

            error = targetPosition + slideMotor.getCurrentPosition();
            if (Math.abs(error) < 50) {
                integral += error;
                integral *= 0.9;
                integral = clamp(integral, -500, 500);
            }
            else {
                integral = 0;
            }

            derivative = error - lastError;
            lastError = error;

            double power = kP * error + kI * integral + kD * derivative;

            slideMotor.setPower(clamp(power, -maxPower, maxPower));

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                slideMotor.setPower(0);
                Logger.warn("Interrupted, setting motor to zero and exiting");
                break;
            }
        }
    }
}