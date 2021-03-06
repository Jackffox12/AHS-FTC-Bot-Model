package edu.ahs.robotics.control;

import java.util.ArrayList;
import java.util.List;

public class Path {
    static final double LOOK_AHEAD_DISTANCE = 6.0; /*Package visible for testing*/
    private final ArrayList<PointAtDistance> pointAtDistance;
    private final double[][] powers;
    private double totalDistance = 0;

    private int iCurrentBound = 0;
    private double initialPower;

    public Path(List<Point> points, boolean flipToBlue, double initialPower, double finalPower, double[][] powers) {
        int finalPowerIndex = powers.length;
        this.powers = new double[finalPowerIndex + 1][2];
        this.initialPower = initialPower;

        if (flipToBlue) {
            for (int i = 0; i < points.size(); i++) {
                Point current = points.get(i);
                current.x = -1.0 * current.x;
            }
        }

        pointAtDistance = new ArrayList<>();
        pointAtDistance.add(new PointAtDistance(points.get(0), 0, 0, 0, 0));

        for (int i = 1; i < points.size(); i++) {
            Point current = points.get(i);
            Point previous = points.get(i - 1);

            if (current.x == previous.x && current.y == previous.y) {
                continue;
            }

            double distanceFromPrevious = current.distanceTo(previous);
            totalDistance += distanceFromPrevious;

            pointAtDistance.add(new PointAtDistance(current, totalDistance, current.x - previous.x, current.y - previous.y, distanceFromPrevious));
        }

        //Add final power to the end of the powers array
        for (int i = 0; i < powers.length; i++) {
            if (powers[i].length != 2) {
                throw new IllegalArgumentException("Length of powers array was not equal to 2");
            }
            this.powers[i] = powers[i];
        }
        this.powers[finalPowerIndex][0] = totalDistance;
        this.powers[finalPowerIndex][1] = finalPower;

        //Check that the distances go in increasing order
        double previousDistanceAtPower = powers[0][0];
        for (int i = 1; i < powers.length; i++) {
            double currentDistAtPower = powers[i][0];
            if (currentDistAtPower < previousDistanceAtPower){
                throw new IllegalArgumentException("Powers supplied to path must be in increasing order. Previous was "+ previousDistanceAtPower +" Current is "+currentDistAtPower);
            }
            previousDistanceAtPower = currentDistAtPower;
        }
    }

    public PointAtDistance getPoint(int index) {
        return pointAtDistance.get(index);
    }

    public boolean isFinished(Position robotPosition) {
        updateFirstBoundingPoint(robotPosition);
        if (iCurrentBound < pointAtDistance.size()-2) {
            return false;
        } else {
            double componentAlongPath = getComponentAlongPath(robotPosition, pointAtDistance.get(pointAtDistance.size() - 1));
            return componentAlongPath <= 0;
        }
    }

    /**
     *
     * @param robotPosition
     * @return Returns a location
     */
    public Location getTargetLocation(Position robotPosition, double lookAheadDistance) {
        //Find closest bounding point behind robot
        updateFirstBoundingPoint(robotPosition);

        //Find the 2 closest bounding points
        PointAtDistance first = getPoint(iCurrentBound);

        PointAtDistance second = getPoint(iCurrentBound + 1);

        Location loc = new Location(second);

        //Find closest point on line to robot
        Line pathLine = new Line(first, second);
        loc.closestPoint = pathLine.getClosestPointOnLine(robotPosition);

        //Calculate distance to end and distance from start of path
        double distanceToSecond = loc.closestPoint.distanceTo(second);
        loc.distanceToEnd = (totalDistance - second.distanceFromStart) + distanceToSecond;
        loc.distanceFromStart = first.distanceFromStart + loc.closestPoint.distanceTo(first);

        //Calculate power at location
        loc.power = getTargetPower(loc.distanceFromStart);

        //Calculate a future point given a look ahead distance
        loc.futurePoint = getFuturePoint(distanceToSecond, lookAheadDistance);

        return loc;
    }

    private Point getFuturePoint(double distanceToSecond, double lookAheadDistance){
        double totalDistance = distanceToSecond;
        int iFutureBound = iCurrentBound + 1;


        // Look for a point on the path that is at least lookAheadDistance from the robot position
        while (totalDistance < lookAheadDistance && iFutureBound < pointAtDistance.size()-1) {
            iFutureBound ++;
            PointAtDistance current = getPoint(iFutureBound);
            totalDistance += current.distanceToPrevious;
        }

        // If we ran out of points, return the last point in the path
        if (totalDistance < lookAheadDistance) {
            return getPoint(iFutureBound);
        }

        // Otherwise, interpolate a point before iFutureBound
        PointAtDistance futureBound = getPoint(iFutureBound);
        double distanceBack = totalDistance - lookAheadDistance;
        double ratio = distanceBack/futureBound.distanceToPrevious;

        return new Point(futureBound.x - (ratio * futureBound.pathDeltaX), futureBound.y - (ratio * futureBound.pathDeltaY));
    }

    private double getTargetPower(double distanceFromStart) {

        double interpolatedPower = initialPower;
        double currentPower;
        for (int i = 0; i < powers.length; i++) {
            if (powers[i][0] > distanceFromStart) {
                break;
            } else {
                currentPower = powers[i][1];
            }
            double nextPower;
            double nextDistance;
            //Check if we are at end
            if (i == powers.length-1) {
                nextPower = powers[i][1];
                nextDistance = powers[i][0];
            } else {
                nextPower = powers[i+1][1];
                nextDistance = powers[i+1][0];
            }
            double powerDifference = nextPower - currentPower;

            double currentDistance = distanceFromStart - powers[i][0];

            double distanceDifference = nextDistance - powers[i][0];
            double distanceRatio;
            if (distanceDifference == 0) {
                distanceRatio = 0;
            } else {
                distanceRatio = currentDistance/distanceDifference;
            }

            interpolatedPower = currentPower + (powerDifference * distanceRatio);
        }

        return interpolatedPower;
    }

    /**
     * Finds the closest point behind the robot in direction of travel and updates the first point
     * @param robotPosition
     */
    private void updateFirstBoundingPoint(Position robotPosition) {

        for (int i = iCurrentBound; i < pointAtDistance.size()-1; i++) {
            PointAtDistance current = getPoint(i);
            double componentToCurrent = getComponentAlongPath(robotPosition, current);
            if (componentToCurrent > 0) {
                break;
            } else {
                iCurrentBound = i;
            }
        }
    }

    /**
     * Takes a position and a path point and finds the component of the distance along the path between the position and the path point
     * @param robotPosition
     * @param pathPoint
     * @return Non-normalized dotProduct
     */
    private double getComponentAlongPath(Position robotPosition, PointAtDistance pathPoint) {
        double robotDeltaX = pathPoint.x - robotPosition.x;
        double robotDeltaY = pathPoint.y - robotPosition.y;

        return (robotDeltaX * pathPoint.pathDeltaX) + (robotDeltaY * pathPoint.pathDeltaY);
    }

    public static class PointAtDistance extends Point {
        private double distanceFromStart;
        private double pathDeltaX;
        private double pathDeltaY;
        private double distanceToPrevious;

        public PointAtDistance(double x, double y, double distanceFromStart, double pathDeltaX, double pathDeltaY, double distanceToPrevious) {
            super(x, y);
            this.distanceFromStart = distanceFromStart;
            this.pathDeltaX = pathDeltaX;
            this.pathDeltaY = pathDeltaY;
            this.distanceToPrevious= distanceToPrevious;

        }

        public PointAtDistance(Point p, double distanceFromStart, double pathDeltaX, double pathDeltaY, double distanceToPrevious) {
            this(p.x, p.y, distanceFromStart, pathDeltaX, pathDeltaY, distanceToPrevious);
        }

    }

    public static class Location {
        public Point closestPoint;
        public double pathDeltaX;
        public double pathDeltaY;
        public double distanceToEnd;
        public double distanceFromStart;
        public double pathSegmentLength;
        public double power;
        public Point futurePoint;

        public Location(PointAtDistance pointAtDistance) {
            pathDeltaX = pointAtDistance.pathDeltaX;
            pathDeltaY = pointAtDistance.pathDeltaY;
            pathSegmentLength = pointAtDistance.distanceToPrevious;
        }
    }

}
