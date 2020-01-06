/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.ahs.robotics.util.opmodes.ardennes;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import edu.ahs.robotics.control.Position;
import edu.ahs.robotics.control.Velocity;
import edu.ahs.robotics.hardware.MecanumChassis;
import edu.ahs.robotics.hardware.sensors.IMU;
import edu.ahs.robotics.hardware.sensors.Odometer;
import edu.ahs.robotics.hardware.sensors.OdometerImpl;
import edu.ahs.robotics.seasonrobots.Ardennes;
import edu.ahs.robotics.util.FTCUtilities;
import edu.ahs.robotics.util.Logger;
import edu.ahs.robotics.util.ParameterLookup;
import edu.ahs.robotics.util.Tuner;
import edu.ahs.robotics.util.opmodes.SimpleTeleOp;


/**
 * Test OpMode for tuning the true diameter of the odometer wheels on Ardennes. The 60mm REV wheels have bad tolerances.
 * This OpMode doesn't utilize the Ardennes class structure and instead directly accesses the encoders via the hardwaremap.
 * You may have to look over this code to make sure it's accurate before using/reusing it.
 * </br>
 * Using this class does require some math. Take the total distance you push your robot and divide it by the amount of rotations to figure out the circumference of the wheel.
 * Knowing the circumference calculate diameter by dividing by pi.
 * The diameter of each wheel should come out to be close to 60mm or 2.36 inches (if you're using the Ardennes rev wheels).
 * @author Alex Appleby
 */
@Autonomous(name="Ardennes Power Curve Tuner", group="Linear Opmode")
//@Disabled
public class ArdennesPowerCurveTuningOpMode extends LinearOpMode {

    //private Ardennes ardennes;
    private Velocity velocity;
    private Ardennes ardennes;
    private MecanumChassis chassis;
    Tuner tuner = new Tuner();
    private double power;
    private double maxSpeed = 0;


    @Override
    public void runOpMode() {
        FTCUtilities.setOpMode(this);
        ardennes = new Ardennes();
        chassis = ardennes.getChassis();

        tuner.addParam("power", .00001);
        tuner.start();
        power = tuner.getParameter("power");

        waitForStart();
        chassis.startOdometrySystem();

        chassis.setPowerAll(power);
        telemetry.addLine(String.valueOf(power));

        while (opModeIsActive()) {
            velocity = chassis.getState().velocity;

            if (velocity.speed() > maxSpeed) {
                maxSpeed = velocity.speed();
            }

            telemetry.addData("maxSpeed", maxSpeed);
            telemetry.update();
        }

        stop();
        chassis.stopOdometrySystem();
    }
}
