package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.kauailabs.navx.ftc.AHRS;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Autonomous(name="Auto: Smash Blue Advanced", group="Main")
public class AutoSmashBlueAdvanced extends AutoFunctions {
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void declareDevices() throws InterruptedException {
        super.declareDevices();
    }

    private AHRS navx_device;

    static double odsStart;

    public void runOpMode() throws InterruptedException {

        declareMap();

        leftFly.setDirection(DcMotor.Direction.REVERSE);
        colorSensor.enableLed(false);

        handFront.setPosition(.5);

        telemetry.addData(">", "Robot Ready.");
        telemetry.addData("White line", odsStart * 3);
        telemetry.update();

        waitForStart();

        runtime.reset();

    }
}