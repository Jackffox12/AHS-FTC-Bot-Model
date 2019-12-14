package edu.ahs.robotics.hardware.sensors;

import org.junit.Test;

import edu.ahs.robotics.util.FTCUtilities;

import static org.junit.Assert.*;

public class OdometrySystemTest {
    private OdometrySystem odometrySystem;

    private void init(double[] y1Inputs, double[] y2Inputs, double[] xInputs){
        FTCUtilities.startTestMode();

        OdometerMock y1 = new OdometerMock(y1Inputs);
        OdometerMock y2 = new OdometerMock(y2Inputs);
        OdometerMock x = new OdometerMock(xInputs);

        odometrySystem = new OdometrySystem(y1, y2, x, .1, 12);
    }

    @Test
    public void driveForward1Foot(){
        double[] y1Inputs = {0,2,8,12}; //OdometrySystem references once upon init - starting with zero is a good idea
        double[] y2Inputs = {0,2,8,12};
        double[] xInputs = {0,0,0,0};
        init(y1Inputs, y2Inputs, xInputs);

        for(int i = 0; i < y1Inputs.length - 1; i++){ //-1 accounts for the constructor getting initial position
            odometrySystem.updatePosition();
        }

        assertEquals(12, odometrySystem.getPosition().y, .01);
        assertEquals(0, odometrySystem.getPosition().heading, .01);
        assertEquals(0, odometrySystem.getPosition().x, .01);
    }

    @Test
    public void turn90Degrees(){
        double[] y1Inputs = {0,0,9.424775};
        double[] y2Inputs = {0,0,-9.424775};
        double[] xInputs = {0,0,9};
        init(y1Inputs,y2Inputs,xInputs);

        for(int i = 0; i < y1Inputs.length - 1; i++){ //-1 accounts for the constructor getting initial position
            odometrySystem.updatePosition();
        }

        assertEquals(90, odometrySystem.getPosition().heading, .01);
        assertEquals(0, odometrySystem.getPosition().x, .01);
        assertEquals(0, odometrySystem.getPosition().y, .01);

    }

    @Test
    public void strafe1Foot(){
        double[] y1Inputs = {0,0,0,0}; //OdometrySystem references once upon init - starting with zero is a good idea
        double[] y2Inputs = {0,0,0,0};
        double[] xInputs = {0,2,4,12};
        init(y1Inputs, y2Inputs, xInputs);

        for(int i = 0; i < y1Inputs.length - 1; i++){ //-1 accounts for the constructor getting initial position
            odometrySystem.updatePosition();
        }

        assertEquals(12, odometrySystem.getPosition().x, .01);
    }

    @Test
    public void strafeAndTurn1Foot(){
        double[] y1Inputs = {0,2,4,12}; //OdometrySystem references once upon init - starting with zero is a good idea
        double[] y2Inputs = {0,2,4,12};
        double[] xInputs = {0,2,4,12};
        init(y1Inputs, y2Inputs, xInputs);

        for(int i = 0; i < y1Inputs.length - 1; i++){ //-1 accounts for the constructor getting initial position
            odometrySystem.updatePosition();
        }

        assertEquals(12, odometrySystem.getPosition().x, .01);
        assertEquals(12, odometrySystem.getPosition().x, .01);
    }

    @Test
    public void turn500Degrees(){
        double[] y1Inputs = {0,0,52.3598611111}; //OdometrySystem references once upon init - starting with zero is a good idea
        double[] y2Inputs = {0,0,-52.3598611111};
        double[] xInputs = {0,0,50};
        init(y1Inputs, y2Inputs, xInputs);

        for(int i = 0; i < y1Inputs.length - 1; i++){ //-1 accounts for the constructor getting initial position
            odometrySystem.updatePosition();
        }

        assertEquals(500, odometrySystem.getPosition().heading, .01);
        assertEquals(0, odometrySystem.getPosition().x, .01);
        assertEquals(0, odometrySystem.getPosition().y, .01);
    }

//    @Test
//    public void pointArc90Deg(){
//        double[] y1Inputs = {0,6,18.84955}; //OdometrySystem references once upon init - starting with zero is a good idea
//        double[] y2Inputs = {0,0,0};
//        double[] xInputs = {0,4.5,9};
//        init(y1Inputs, y2Inputs, xInputs);
//
//        for(int i = 0; i < y1Inputs.length - 1; i++){ //-1 accounts for the constructor getting initial position
//            odometrySystem.updatePosition();
//        }
//
//        assertEquals(90, odometrySystem.getPosition().heading, .1);
//        //assertEquals(-10.392, odometrySystem.getPosition().x, .05);
//        assertEquals((7.63944)/2, odometrySystem.getPosition().y, .1);
//    }

}