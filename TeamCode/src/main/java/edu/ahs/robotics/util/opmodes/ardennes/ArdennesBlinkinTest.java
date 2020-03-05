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
 * ARE DISCLAIMED. INSLOW NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER INSLOW CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING INSLOW ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.ahs.robotics.util.opmodes.ardennes;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.TouchSensor;

import edu.ahs.robotics.hardware.sensors.IMU;
import edu.ahs.robotics.hardware.sensors.TriggerDistanceSensor;
import edu.ahs.robotics.seasonrobots.Ardennes;
import edu.ahs.robotics.util.ftc.FTCUtilities;

/**
 * An opmode for logging various sensors. Adjust to your will.
 */
@TeleOp(name="Ardennes Blinkin", group="Iterative Opmode")
//@Disabled
public class ArdennesBlinkinTest extends OpMode
{
    Ardennes ardennes;
    RevBlinkinLedDriver blinkinLedDriver;
    RevBlinkinLedDriver.BlinkinPattern pattern;

    @Override
    public void init() {
        FTCUtilities.setOpMode(this);
        ardennes = new Ardennes();
        blinkinLedDriver = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
    }

    @Override
    public void loop() {
        if (gamepad1.x) {
            blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);
        }

        if (gamepad1.y) {
            blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.GREEN);
        }

        if (gamepad1.a) {
            blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE);
        }
    }

    @Override
    public void stop() {
    }

}
