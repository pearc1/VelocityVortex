/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.kauailabs.navx.ftc.AHRS;
import com.kauailabs.navx.ftc.navXPIDController;
import com.qualcomm.ftccommon.FtcWifiChannelSelectorActivity;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcontroller.external.samples.ConceptTelemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.text.DecimalFormat;

@TeleOp(name="SensorTest", group="Iterative Opmode")
public class SensorTest extends OpMode {
    /* Declare OpMode members. */

    // Device declarations
    private DcMotor leftMotor = null;
    private DcMotor rightMotor = null;
    private DcMotor elevator = null;
    private DcMotor leftFly = null;
    private DcMotor rightFly = null;
    private Servo handFront = null;

    TouchSensor touchSensor = null;
    TouchSensor elevatorTouch = null;
    OpticalDistanceSensor odsSensor;
    ColorSensor colorSensor = null;
    ModernRoboticsI2cRangeSensor ultra = null;
    OpticalDistanceSensor sharpIR = null;

    final int NAVX_DIM_I2C_PORT = 0;
    AHRS navx_device;
    navXPIDController yawPIDController;
    ElapsedTime runtime = new ElapsedTime();

    final byte NAVX_DEVICE_UPDATE_RATE_HZ = 50;

    double TARGET_ANGLE_DEGREES = 0.0;
    final double TOLERANCE_DEGREES = 1.0;
    final double MIN_MOTOR_OUTPUT_VALUE = -1.0;
    final double MAX_MOTOR_OUTPUT_VALUE = 1.0;
    final double YAW_PID_P = 0.005;
    final double YAW_PID_I = 0.0;
    final double YAW_PID_D = 0.0;

    boolean calibration_complete = false;
    boolean navxConnected = true;

    navXPIDController.PIDResult yawPIDResult;
    DecimalFormat df;
    String color;

   static double odsStart;


    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {
        telemetry.addData("Status", "Initialized");
        // get motors
        leftMotor = hardwareMap.dcMotor.get("leftMotor");
        rightMotor = hardwareMap.dcMotor.get("rightMotor");
        elevator = hardwareMap.dcMotor.get("elevator");
        leftFly = hardwareMap.dcMotor.get("leftFly");
        rightFly = hardwareMap.dcMotor.get("rightFly");

        handFront = hardwareMap.servo.get("handFront");

        // Reverse the motor that runs backwards when connected directly to the battery
        leftMotor.setDirection(DcMotor.Direction.FORWARD); // Set to REVERSE if using AndyMark motors
        rightMotor.setDirection(DcMotor.Direction.REVERSE);// Set to FORWARD if using AndyMark motors

        leftFly.setDirection(DcMotor.Direction.REVERSE);

        odsSensor = hardwareMap.opticalDistanceSensor.get("ods");
        touchSensor = hardwareMap.touchSensor.get("touchSensor");
        colorSensor = hardwareMap.colorSensor.get("colorSensor");
        elevatorTouch = hardwareMap.touchSensor.get("elevatorTouch");
        ultra = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "ultra");
        sharpIR = hardwareMap.opticalDistanceSensor.get("infrared");

        {
            navx_device = AHRS.getInstance(hardwareMap.deviceInterfaceModule.get("dim"),
                    NAVX_DIM_I2C_PORT,
                    AHRS.DeviceDataType.kProcessedData,
                    NAVX_DEVICE_UPDATE_RATE_HZ);
            // Create a PID Controller which uses the Yaw Angle as input.
            yawPIDController = new navXPIDController( navx_device,
                    navXPIDController.navXTimestampedDataSource.YAW);

            // Configure the PID controller
            yawPIDController.setSetpoint(TARGET_ANGLE_DEGREES);
            yawPIDController.setContinuous(true);
            yawPIDController.setOutputRange(MIN_MOTOR_OUTPUT_VALUE, MAX_MOTOR_OUTPUT_VALUE);
            yawPIDController.setTolerance(navXPIDController.ToleranceType.ABSOLUTE, TOLERANCE_DEGREES);
            yawPIDController.setPID(YAW_PID_P, YAW_PID_I, YAW_PID_D);
            yawPIDController.enable(true);
        }

        odsStart = odsSensor.getLightDetected();

        colorSensor.enableLed(false);

        telemetry.addData("Status", "Initialized");
    }

    @Override
    public void init_loop() {
    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */

    @Override
    public void start() {
        runtime.reset();
    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {

        if ((colorSensor.red() > colorSensor.blue()) && (colorSensor.red() > colorSensor.alpha())) {
            color = "Red";
        }
        else if ((colorSensor.blue() > colorSensor.red()) && (colorSensor.blue() > colorSensor.alpha())) {
            color = "Blue";
        }
        else {
            color = "Alpha";
        }

        // end of code, update telemetry
        telemetry.addData("UltraSonic Raw: ", ultra.rawUltrasonic());
        telemetry.addData("cm", "%.2f cm", ultra.getDistance(DistanceUnit.CM));
        telemetry.addData("Navx: ", navx_device.getYaw());
        telemetry.addData("SharpIR: ", sharpIR.getLightDetected());
        telemetry.addData("Color Red: ", color);
        telemetry.addData("Elevator Touch: ", elevatorTouch.getValue());
        telemetry.update();
    }

    @Override
    public void stop() {
    }
}
