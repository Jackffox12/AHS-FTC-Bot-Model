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

    //Correction values
    private static final double TURN_SCALE = .01;
    private static final double LOOK_AHEAD_TIME = 0.1; //note that this is in seconds, not millis due to speed and acceleration units.

    public HeadingController(Path path, double maxPower) {
        this.path = path;
        this.maxPower = maxPower;

        ParameterLookup lookup = FTCUtilities.getParameterLookup();

        double p = lookup.getParameter("p");
        double d = lookup.getParameter("d");

        speedPID = new PID(.004, 0.0, .006, 5); // -- tuned --
        unifiedPID = new PID(p, 0.0, d,5);

        logger.startWriting();
    }

    public Powers getUpdatedPowers(OdometrySystem.State robotState) {
        Position robotPosition = robotState.position;
        Velocity robotVelocity = robotState.velocity;

        Path.Location targetLocation = path.getTargetLocation(robotPosition);

        if (!targetLocation.pathFinished) {
            double targetSpeed = targetLocation.lookAheadSpeed;

            double speedAlongPath = (robotVelocity.dx * targetLocation.pathDeltaX) + (robotVelocity.dy * targetLocation.pathDeltaY);
            speedAlongPath /= targetLocation.pathSegmentLength;

            PID.Corrections speedCorrections = speedPID.getCorrection(targetSpeed - speedAlongPath);
            double totalSpeedCorrection = speedCorrections.totalCorrection;
            if (totalSpeedCorrection < 0) {
                totalSpeedCorrection *= DOWN_AMPLIFIER;
            }

            leftPower += totalSpeedCorrection;
            rightPower += totalSpeedCorrection;

            Point futurePoint = getFuturePoint(robotState, LOOK_AHEAD_TIME);
            Position futurePosition = new Position(futurePoint,0);

            Path.Location futureTarget = path.getTargetLocation(futurePosition);
            Point futureTargetPoint = futureTarget.closestPoint;

            double error = futurePoint.distanceTo(futureTargetPoint);
            PID.Corrections unifiedCorrections = unifiedPID.getCorrection(error);

            leftPower -= unifiedCorrections.totalCorrection; //todo fix onesidedness
            rightPower += unifiedCorrections.totalCorrection;

            logger.append("targetSpeed", String.valueOf(targetSpeed));
            logger.append("robotSpeed", String.valueOf(robotVelocity.speed()));
            //logger.append("speedCorrection", String.valueOf(totalSpeedCorrection));
            //logger.append("speedCorrectionP", String.valueOf(speedCorrections.correctionP));
            //logger.append("speedCorrectionI", String.valueOf(speedCorrections.correctionI));
            //logger.append("speedCorrectionD", String.valueOf(speedCorrections.correctionD));
            //logger.append("speedAlongPath", String.valueOf(speedAlongPath));
            //logger.append("distanceToRobot", String.valueOf(targetLocation.distanceToRobot));
            //logger.append("distanceToEnd", String.valueOf(targetLocation.distanceToEnd));
            logger.append("robotPositionX", String.valueOf(robotPosition.x));
            logger.append("robotPositionY", String.valueOf(robotPosition.y));
            logger.append("robotPositionHeading", String.valueOf(robotPosition.heading));
            logger.append("path Direction", String.valueOf(targetLocation.pathDirection));
            //logger.append("closestPointX", String.valueOf(targetLocation.closestPoint.x));
            //logger.append("closestPointY", String.valueOf(targetLocation.closestPoint.y));

//            logger.append("turnCorrection", String.valueOf(totalTurnCorrection));
//            logger.append("turnCorrectionP", String.valueOf(positionCorrections.correctionP));
//            logger.append("turnCorrectionI", String.valueOf(positionCorrections.correctionI));
//            logger.append("turnCorrectionD", String.valueOf(positionCorrections.correctionD));
            logger.append("lookAheadCurvature", String.valueOf(targetLocation.lookAheadCurvature));
            logger.append("robotCurvature", String.valueOf(robotState.travelRadius));

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

        } else {
            leftPower = 0.0;
            rightPower = 0.0;
        }

        logger.append("leftPower", String.valueOf(leftPower));
        logger.append("rightPower", String.valueOf(rightPower));
        logger.append("isFinished", String.valueOf(targetLocation.pathFinished));
        logger.writeLine();

        if (targetLocation.pathFinished) {
            logger.stopWriting();
        }

        return new Powers(leftPower, rightPower, targetLocation.pathFinished);
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
