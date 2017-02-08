package org.firstinspires.ftc.teamcode;

import com.kauailabs.navx.ftc.AHRS;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name="Auto: Smash Blue", group="Main")
public class AutoSmashBlue extends AutoFunctions {
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

        runForTime(-.3, -.3, 500);

        // fire first ball
        telemetry.addData("AutoStatus: ", "Firing first ball");
        telemetry.update();
        autoFire();
        elevatorDown();

        // load second ball
        telemetry.addData("AutoStatus: ", "Loading second ball");
        telemetry.update();
        handFront.setPosition(1);
        sleep(500);

        // fire second ball
        telemetry.addData("AutoStatus: ", "Firing second ball");
        telemetry.update();
        autoFire2();
        elevatorDownDrive(.8, .2, -.2);

        findWhite();

        runForTime(-.125, -.125, 300);

        turnToWhite(0, -.25);

        stayWhiteBlueAdvanced();
        runForTime(.1, .1, 500);
        sleep(500);

        runtime.reset();
        while (opModeIsActive() && runtime.time() < 2) {
            if (colorSensor.red() > colorSensor.blue() && !(colorSensor.red() < colorSensor.alpha())) {
                runForTime(-.1, -.1, 1000);
            }
            else if (colorSensor.alpha() > colorSensor.red()) {
                runForTime(-.1, -.1, 500);
            }
            else {
                leftMotor.setPower(0);
                rightMotor.setPower(0);
            }
        }


    }
}