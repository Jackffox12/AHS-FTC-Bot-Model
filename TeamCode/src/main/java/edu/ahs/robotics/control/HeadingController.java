package edu.ahs.robotics.control;

import edu.ahs.robotics.control.pid.PID;
import edu.ahs.robotics.hardware.sensors.OdometrySystem;
import edu.ahs.robotics.util.FTCUtilities;
import edu.ahs.robotics.util.Logger;
import edu.ahs.robotics.util.ParameterLookup;

public class HeadingController {
    //Amplifies negative power corrections to deal with momentum while decelerating
    private static final double DOWN_AMPLIFIER = 1.5; // -- tuned --
    Path path;
    Logger logger = new Logger("TestAutoData");
    double downCorrectionScale;
    private PID speedPID;
    private PID unifiedPID;
    private double maxPower;
    private double leftPower = .2;
    private double rightPower = .2;

    private long lastTime;

    //Correction values
    private static final double TURN_SCALE = .01;
    private static final double LOOK_AHEAD_TIME = 0.3; //note that this is in seconds, not millis due to speed and acceleration units.
    private static final double PID_CONSTANT_SCALAR = 0.001;// so that actual tuning values can be more fathomable to the reader

    public HeadingController(Path path, double maxPower) {
        this.path = path;
        this.maxPower = maxPower;

        ParameterLookup lookup = FTCUtilities.getParameterLookup();

        double p = lookup.getParameter("p");
        double d = lookup.getParameter("d");

        speedPID = new PID(0.01*PID_CONSTANT_SCALAR, 0.0, 0.015*PID_CONSTANT_SCALAR, 5); // -- tuned --
        unifiedPID = new PID(p * PID_CONSTANT_SCALAR, 0.0, d * PID_CONSTANT_SCALAR,5);

        logger.startWriting();

        lastTime = FTCUtilities.getCurrentTimeMillis();
    }

    /**
     * Given a robot state, calculate speed and turning errors and update powers accordingly.
     * @param robotState
     * @return
     */
    public Powers getUpdatedPowers(OdometrySystem.State robotState) {
        long time = FTCUtilities.getCurrentTimeMillis();
        long deltaTime = time - lastTime;

        Position robotPosition = robotState.position;
        Velocity robotVelocity = robotState.velocity;

        if (!path.isFinished(robotPosition)) {

            //Find the future point where the robot will be LOOK_AHEAD_TIME in the future
            Point futurePoint = getFuturePoint(robotState, LOOK_AHEAD_TIME);
            Position futurePosition = new Position(futurePoint,0);

            //Find closest point on path to future point
            Path.Location targetLocation = path.getTargetLocation(futurePosition);

            //Use PID to calculate speed correction
            PID.Corrections speedCorrections = speedPID.getCorrection(targetLocation.speed - robotState.velocity.speed(), deltaTime);
            double totalSpeedCorrection = speedCorrections.totalCorrection;

            //Use a down Amplifier to increase weight on negative speed corrections
            if (totalSpeedCorrection < 0) {
                totalSpeedCorrection *= DOWN_AMPLIFIER;
            }

            leftPower += totalSpeedCorrection;
            rightPower += totalSpeedCorrection;

            //Turn error.
            double error = targetLocation.distanceToRobot; //signed distance where positive is robot to right of path

            //Create PID controller for turning error. Positive corrections mean turn left.
            PID.Corrections unifiedCorrections = unifiedPID.getCorrection(error,deltaTime);

            leftPower -= unifiedCorrections.totalCorrection;
            rightPower += unifiedCorrections.totalCorrection;

            logger.append("targetSpeed", String.valueOf(targetLocation.speed));
            logger.append("robotSpeed", String.valueOf(robotVelocity.speed()));

            logger.append("robotPositionX", String.valueOf(robotPosition.x));
            logger.append("robotPositionY", String.valueOf(robotPosition.y));
            logger.append("robotPositionHeading", String.valueOf(robotPosition.heading));

            logger.append("futureX", String.valueOf(futurePoint.x));
            logger.append("futureY", String.valueOf(futurePoint.y));

            logger.append("error", String.valueOf(error));

            logger.append("acceleration", String.valueOf(robotState.acceleration));
            logger.append("travel radius", String.valueOf(robotState.travelRadius));

            logger.append("power correction", String.valueOf(unifiedCorrections.totalCorrection));

            //Clip powers to maxPower by higher power
            double higherPower = Math.max(Math.abs(leftPower), Math.abs(rightPower));
            if (higherPower > maxPower) {
                leftPower = (leftPower / higherPower) * maxPower;
                rightPower = (rightPower / higherPower) * maxPower;
            }

            if (leftPower < .05) {
                leftPower = .05;
            }
            if (rightPower < .05) {
                rightPower = .05;
            }


        }
        //If the path is finished, stop motors
        else {
            leftPower = 0.0;
            rightPower = 0.0;
        }

        logger.append("leftPower", String.valueOf(leftPower));
        logger.append("rightPower", String.valueOf(rightPower));
        logger.append("isFinished", String.valueOf(path.isFinished(robotPosition)));
        logger.writeLine();

        if (path.isFinished(robotPosition)) {
            logger.stopWriting();
        }

        lastTime = time;
        return new Powers(leftPower, rightPower, path.isFinished(robotPosition));
    }

    /**
     * Gets a projected point estimated by the current state of the robot. Useful for PID and stuff.
     * Protected for unit testing.
     * @param robotState The current state of the robot. Note that radius is signed.
     * @param lookAheadTime time in seconds to look ahead on the path.
     * @return An estimation of where the robot will be lookAheadTime seconds in the future.
     */
    /*protected for testing*/ Point getFuturePoint(OdometrySystem.State robotState, double lookAheadTime){

        double distance = (robotState.velocity.speed() * lookAheadTime) + (.5) * (robotState.acceleration * (lookAheadTime * lookAheadTime)); // suvat, ut * 1/2at^2
        Vector h = Vector.makeUnitVector(robotState.position.heading); //make a unit vector in the direction of heading

        if(robotState.travelRadius == Double.POSITIVE_INFINITY || robotState.travelRadius == Double.NEGATIVE_INFINITY){
            h.scale(distance);

            return new Point(robotState.position.x + h.x, robotState.position.y + h.y);
        } else {

            Vector perp = h.getPerpVector(); // Note that this is always leftward relative to robot
            perp.scale(robotState.travelRadius); // a negative radius (aka traveling right) will invert this vector rightward.

            double centerX = robotState.position.x + perp.x; //todo check w john
            double centerY = robotState.position.y + perp.y;

            double dx = (robotState.position.x - centerX); //effectively the unit circle components for use to derive an angle using atan2.
            double dy = (robotState.position.y - centerY);

            if(dy == 0 && dx == 0){
                return new Point(centerX, centerY);
            }

            double angleToCurrentPos = Math.atan2(dy, dx);

            double angleCurrentToTarget = distance / robotState.travelRadius;//l = theta * r. Signed radius checks out, rightward angle is globally negative when added in next line

            double angleToFuturePoint = angleToCurrentPos + angleCurrentToTarget;

            double x = centerX + (Math.abs(robotState.travelRadius) * Math.cos(angleToFuturePoint)); //note the absolute value on the radius
            double y = centerY + (Math.abs(robotState.travelRadius) * Math.sin(angleToFuturePoint)); //since we're measuring this point relative to the center of the circle in global coords, we don't care about directionality

            return new Point(x, y);
        }
    }

    /**
     * Finds direction error ensuring appropriate angle wrapping.
     */
    private double getDirectionError(double targetDirection, double currentDirection){
        double error = targetDirection - currentDirection;

        if(error > Math.PI){
            error -= (2 * Math.PI);
        } else if (error < -Math.PI){
            error += (2 * Math.PI);
        }

        return error;
    }

    public static class Powers {
        public double leftPower;
        public double rightPower;
        public boolean pathFinished;

        public Powers(double leftPower, double rightPower, boolean pathFinished) {
            this.leftPower = leftPower;
            this.rightPower = rightPower;
            this.pathFinished = pathFinished;
        }
    }
}