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

package org.firstinspires.ftc.teamcode.old;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import edu.ahs.robotics.hardware.MecanumChassis;
import edu.ahs.robotics.hardware.sensors.ArdennesSkyStoneDetector;
import edu.ahs.robotics.hardware.sensors.TriggerDistanceSensor;
import edu.ahs.robotics.seasonrobots.Ardennes;
import edu.ahs.robotics.util.ftc.FTCUtilities;
import edu.ahs.robotics.util.MotorHashService;
import edu.ahs.robotics.util.ftc.Tuner;


@Autonomous(name = "Old Code Test Auto", group = "Linear Opmode")
@Disabled
public class OldCodeTestAuto extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    private Ardennes ardennes;
    private Tuner tuner;
    private ArdennesSkyStoneDetector detector;
    private TriggerDistanceSensor intakeTrigger;

    @Override
    public void runOpMode() {

        FTCUtilities.setOpMode(this);
        MotorHashService.init();
        tuner = new Tuner();
        FTCUtilities.setParameterLookup(tuner);
        ardennes = new Ardennes();
        detector = new ArdennesSkyStoneDetector(false, true);
//        Intake intake = ardennes.getIntake();
        MecanumChassis chassis = ardennes.getChassis();
        /*Slides slides = ardennes.getSlides();
        SerialServo foundationServoLeft = ardennes.getLeftFoundation();
        SerialServo foundationServoRight = ardennes.getRightFoundation();
        SerialServo gripper = ardennes.getGripper();
        SerialServo yslide = ardennes.getxSlide();
        TriggerDistanceSensor gripperTrigger = ardennes.getGripperTrigger();
        TriggerDistanceSensor intakeTrigger = ardennes.getIntakeTrigger();
        slides.resetEncoders();
        gripper.setPosition(0);
        foundationServoLeft.setPosition(0);
        foundationServoRight.setPosition(0);
        yslide.setPosition(0);*/

        waitForStart();
        chassis.startOdometrySystem();

        chassis.driveStraight(60, .8);

//        chassis.arc(30, 24, .5, true);
//        sleep(1000);
//        chassis.arc(-30, 24, .5, true);
//        sleep(1000);
//        chassis.arc(30, 24, .5, false);
//        sleep(1000);
//        chassis.arc(-30, 24, .5, false);

//        chassis.pivot(90, .7);
//        FTCUtilities.sleep(1000);
//        chassis.pivot(-90, .7);
//        FTCUtilities.sleep(1000);
//        chassis.driveStraight(100, .8);
//        FTCUtilities.sleep(1000);
//        chassis.driveStraight(-36, .93);
//        FTCUtilities.sleep(1000);
//        chassis.arc(90, 24, .93, true);
//        FTCUtilities.sleep(1000);
//        chassis.arc(90, 24, .93, false);


        stop();
        chassis.stopOdometrySystem();

        //ArdennesSkyStoneDetector.SkyStoneConfigurations stoneConfiguration = detector.look(false);
        //sleep(10000);
        //chassis.arc(90, 1000, .93, true);
        //intake.startIntakeWaitForBlock(gripperTrigger);
        //sleep(5000);
        //chassis.pivot(10, .93);

      /*  intake.startIntakeWaitForBlock(intakeTrigger);
        chassis.arc(40,1450, .65, false);
        chassis.arc(-46, 1500, .8, true);
        chassis.driveStraight(-800,.8);
        sleep(300);
        chassis.pivot(-90, .7);
        sleep(300);
        chassis.driveStraight(-200,.8);
        foundationServoLeft.setPosition(1);
        foundationServoRight.setPosition(1);
        sleep(500);
        chassis.arc(90,80,1,true);
        sleep(300);
        chassis.driveStraight(-200, 1);*/



       /* slides.setTargetLevel(2);
        slides.runSlidesToTargetLevel();
        sleep(300);
        yslide.setPosition(1);
        sleep(1500);
        gripper.setPosition(0);
        sleep(1000);
        yslide.setPosition(0);
        sleep(1000);
        slides.resetSlidesToOriginalPosition();*/


    }
}
