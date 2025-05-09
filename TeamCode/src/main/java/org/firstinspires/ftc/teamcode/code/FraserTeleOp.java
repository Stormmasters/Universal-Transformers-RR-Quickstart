package org.firstinspires.ftc.teamcode.code;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.utils.functions.ChassisController;
import org.firstinspires.ftc.teamcode.utils.functions.Extension;
import org.firstinspires.ftc.teamcode.utils.functions.Lift;
import org.firstinspires.ftc.teamcode.utils.functions.Logger;

import java.util.List;

// WAIT!!! Before you modify this code, remember:
// You can't access motors defined in separate threads (DO NOT ATTEMPT THIS, IT WILL CAUSE MAJOR PROBLEMS)

@TeleOp(name = "FraserTeleOp Version 0.5")
public class FraserTeleOp extends OpMode {
    private Lift lift = new Lift();
    private Extension extension = new Extension();
    private ChassisController chassis = new ChassisController();
    private DcMotorEx intakeMotor, hangMotor;
    double sensitivity = 1;
    private boolean lBumper = false, rBumper = false;
    private DcMotorEx FL, BL, FR, BR;

    @Override
    public void init() {
        Logger.disable();
        Logger.info("TeleOp Initialized");
        lift.initialize(hardwareMap);
        chassis.initialize(hardwareMap);
        extension.initialize(hardwareMap);
        intakeMotor = hardwareMap.get(DcMotorEx.class, "IM");
        intakeMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        intakeMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        hangMotor = hardwareMap.get(DcMotorEx.class, "HM");
        hangMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        hangMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        hangMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        FL = hardwareMap.get(DcMotorEx.class, "FL");
        BL = hardwareMap.get(DcMotorEx.class, "BL");
        FR = hardwareMap.get(DcMotorEx.class, "FR");
        BR = hardwareMap.get(DcMotorEx.class, "BR");
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule module : allHubs) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }
    }

    @Override
    public void loop() {
        chassis.update(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, sensitivity);
        if (gamepad1.right_bumper) {
            if (lift.isExtended() && !rBumper){
                lift.retract();
            }
            else if (!rBumper){
                lift.extend();
            }
            rBumper = true;
            gamepad1.rumbleBlips(2);
        }
        else {
            rBumper = false;
        }
        if (gamepad1.left_bumper) {
            if (extension.isExtended() && !lBumper){
                extension.retract();
            }
            else if (!lBumper){
                extension.extend();
            }
            lBumper = true;
            gamepad1.rumbleBlips(4);
        }
        else {
            lBumper = false;
        }
        if (gamepad1.dpad_up && sensitivity < 1){
            sensitivity += 0.01;
        }
        if (gamepad1.dpad_down && sensitivity > 0.1){
            sensitivity -= 0.01;
        }
        if (gamepad1.dpad_left){
            hangMotor.setPower(1);
        }
        else if (gamepad1.dpad_right){
            hangMotor.setPower(-1);
        }
        else {
            hangMotor.setPower(0);
        }
        intakeMotor.setPower(gamepad1.left_trigger - gamepad1.right_trigger);
        telemetry.addLine("Intake slide position: " + extension.getSlidePosition());
        telemetry.addLine("Intake slide power: " + extension.getSlidePower());
        telemetry.addLine("Intake target position: " + extension.getTargetPosition());
        telemetry.addLine("Intake slide mode: " + extension.getSlideMode());
        telemetry.addLine("Slide position: " + lift.getSlidePosition());
        telemetry.addLine("Slide power: " + lift.getSlidePower());
        telemetry.addLine("Slide mode: " + lift.getSlideMode());
        telemetry.update();
    }

    @Override
    public void stop() {
        Logger.info("Asking Slides to terminate threads...");
        lift.stopSlideThreads();
        extension.stopIntakeThread();
    }
}