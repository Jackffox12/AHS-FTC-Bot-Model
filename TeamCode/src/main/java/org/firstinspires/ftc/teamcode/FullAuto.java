package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.ElapsedTime;

import edu.ahs.robotics.hardware.Intake;
import edu.ahs.robotics.hardware.MecanumChassis;
import edu.ahs.robotics.hardware.SerialServo;
import edu.ahs.robotics.hardware.Slides;
import edu.ahs.robotics.hardware.sensors.ArdennesSkyStoneDetector;
import edu.ahs.robotics.hardware.sensors.TriggerDistanceSensor;
import edu.ahs.robotics.seasonrobots.Ardennes;
import edu.ahs.robotics.util.FTCUtilities;
import edu.ahs.robotics.util.MotorHashService;

public class FullAuto {

    private ElapsedTime runtime = new ElapsedTime();
    private Ardennes ardennes;
    private Intake intake;
    private MecanumChassis chassis;
    private Slides slides;
    private SerialServo foundationServoLeft;
    private SerialServo foundationServoRight;
    private SerialServo gripper;
    private SerialServo yslide;
    private TriggerDistanceSensor gripperTrigger;
    private ArdennesSkyStoneDetector detector;
    private boolean mirrored;
    private ArdennesSkyStoneDetector.SkyStoneConfigurations stoneConfiguration;

    public FullAuto(boolean mirrored) {
        this.mirrored = mirrored;
    }

    public void init() {

        MotorHashService.init();
        ardennes = new Ardennes();
        detector = new ArdennesSkyStoneDetector();
        intake = ardennes.getIntake();
        chassis = ardennes.getChassis();
        slides = ardennes.getSlides();
        foundationServoLeft = ardennes.getLeftFoundation();
        foundationServoRight = ardennes.getRightFoundation();
        gripper = ardennes.getGripper();
        yslide = ardennes.getySlide();
        gripperTrigger = ardennes.getGripperTrigger();
        slides.resetEncoders();
        gripper.setPosition(0);
        foundationServoLeft.setPosition(0);
        foundationServoRight.setPosition(0);
        yslide.setPosition(0);
        stoneConfiguration = detector.look(mirrored);
    }

    public void afterStart() {

        if (ArdennesSkyStoneDetector.SkyStoneConfigurations.ONE_FOUR == stoneConfiguration) {
            leftPlan();
        } else if (ArdennesSkyStoneDetector.SkyStoneConfigurations.TWO_FIVE == stoneConfiguration) {
            middlePlan();
        } else rightPlan();

    }

    private void leftPlan() {
        //pivot(-15, .93);
        arc(15,1300, .93, false);
        intake.startIntakeWaitForBlock(gripperTrigger);
//        chassis.driveStraight(700, .93);
//        chassis.driveStraight(400, .65);
        chassis.driveStraight(-250, .93);
        arc(-67,500,.93,true);
        FTCUtilities.sleep(500);
        chassis.driveStraight(-800, .85);
        FTCUtilities.sleep(500);
        pivot(-70, .93);
        chassis.driveStraight(-200, .65);
        foundationServoLeft.setPosition(1);
        foundationServoRight.setPosition(1);
        FTCUtilities.sleep(500);
//        arc(90, 300, .93, true);

        /*chassis.driveStraight(500, 1);
        chassis.pivot(-30, .4);
        intake.startIntakeWaitForBlock(ardennes.getIntakeTrigger());
        chassis.driveStraight(450, .3);
        */

    }

    private void middlePlan() {
        pivot(-9, .93);
        intake.startIntakeWaitForBlock(gripperTrigger);
        chassis.driveStraight(900, .7);

        /*pivot(10, .4);
        chassis.driveStraight(600, 1);
        pivot(-40, .4);
        intake.startIntakeWaitForBlock(ardennes.getIntakeTrigger());
        chassis.driveStraight(450, .3);
        chassis.driveStraight(-450, 1);
        pivot(-57, .4);
        chassis.driveStraight(-1700, .8);
        pivot(-87, .5);
        chassis.driveStraight(-200, .4);
        foundationServoLeft.setPosition(0);
        foundationServoRight.setPosition(1);
        FTCUtilities.sleep(1500);
        chassis.driveStraight(700, 1);
        pivot(93, .5);
        foundationServoLeft.setPosition(1);
        foundationServoRight.setPosition(1);
        FTCUtilities.sleep(700);
        */

    }

    private void rightPlan() {
        pivot(5, .93);
        intake.startIntakeWaitForBlock(gripperTrigger);
        chassis.driveStraight(950, .7);

        /*chassis.pivot(-10, .4);
        chassis.driveStraight(550, 1);
        chassis.pivot(30, .5);
        intake.startIntakeWaitForBlock(ardennes.getIntakeTrigger());
        chassis.driveStraight(300, .3);
        */

    }

    private void pivot(double angle, double maxPower) {
        if (mirrored) {
            chassis.pivot(-angle, maxPower);
        } else {
            chassis.pivot(angle, maxPower);
        }

    }

    private void arc(double angle, double radius, double maxPower, boolean rightTurn) {
        if (mirrored) {
            chassis.arc(angle, radius, maxPower, !rightTurn);
        } else {
            chassis.arc(angle, radius, maxPower, rightTurn);
        }
    }
}

