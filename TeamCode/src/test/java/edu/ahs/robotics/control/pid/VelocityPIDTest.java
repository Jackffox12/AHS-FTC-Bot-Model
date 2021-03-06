package edu.ahs.robotics.control.pid;

import org.junit.Test;

import edu.ahs.robotics.control.Velocity;

import static org.junit.Assert.*;

public class VelocityPIDTest {
    private VelocityPID pid;

    private void init(VelocityPID.Config config){
        pid = new VelocityPID(config);
    }

    @Test
    public void testZeroCase(){
        VelocityPID.Config config = new VelocityPID.Config();
        config.setSpeedParams(1,1,-1);
        config.setDirectionParams(1,1,-1);

        init(config);
        Velocity velocity = Velocity.makeVelocityFromSpeedDirection(1,1); //new Velocity(1, 1);

        VelocityPID.Correction correction = pid.getCorrection(velocity, velocity,0);
        assertEquals(0,correction.speed,0);
        assertEquals(0,correction.direction,0);
    }

    @Test
    public void testProportionality(){
        VelocityPID.Config config = new VelocityPID.Config();
        config.setSpeedParams(1,1,-1);
        config.setDirectionParams(1,1,-1);

        init(config);
        Velocity velocity = Velocity.makeVelocityFromSpeedDirection(1,1);//new Velocity(1, 1);
        Velocity wrongVelocity = Velocity.makeVelocityFromSpeedDirection(2,2);//new Velocity(2, 2);
        Velocity wrongerVelocity = Velocity.makeVelocityFromSpeedDirection(10,10);// new Velocity(10,10);

        VelocityPID.Correction smolCorrection = pid.getCorrection(velocity, wrongVelocity,0);
        VelocityPID.Correction thicCorrection = pid.getCorrection(velocity, wrongerVelocity,0);

        assertTrue(thicCorrection.direction > smolCorrection.direction);
        assertTrue(thicCorrection.speed > smolCorrection.speed);
    }

    @Test
    public void testAllZeroCoefficients(){
        VelocityPID.Config config = new VelocityPID.Config();
        config.setSpeedParams(0,0,0);
        config.setDirectionParams(0,0,0);

        init(config);
        Velocity velocity = Velocity.makeVelocityFromSpeedDirection(1,1); //new Velocity(1, 1);
        Velocity veryWrongVelocity = Velocity.makeVelocityFromSpeedDirection(100,100); //new Velocity(100,100);

        VelocityPID.Correction thicCorrection = pid.getCorrection(velocity, veryWrongVelocity,0);

        assertEquals(0,thicCorrection.direction,0);
        assertEquals(0,thicCorrection.speed,0);

    }

    @Test
    public void testIntegral(){
        VelocityPID.Config config = new VelocityPID.Config();
        config.setSpeedParams(0,1,0);
        config.setDirectionParams(0,1,0);

        init(config);
        Velocity velocity = Velocity.makeVelocityFromSpeedDirection(1,1);// new Velocity(1, 1);
        Velocity wrongVelocity = Velocity.makeVelocityFromSpeedDirection(2,2);// new Velocity(2, 2);

        for(int i = 0; i < 10; i++){ //wind up the integral term
            pid.getCorrection(velocity, wrongVelocity,0);
        }

        VelocityPID.Correction correction = pid.getCorrection(velocity, wrongVelocity,0);

        assertTrue(correction.speed > 0);
        assertTrue(correction.direction> 0);
    }

    @Test
    public void testDerivative(){
        VelocityPID.Config config = new VelocityPID.Config();
        config.setSpeedParams(0,0,1);
        config.setDirectionParams(0,0,1);

        init(config);

        Velocity velocity = Velocity.makeVelocityFromSpeedDirection(1,1);//new Velocity(1, 1);
        Velocity farVelocity = Velocity.makeVelocityFromSpeedDirection(10,10);//new Velocity(10, 10);
        Velocity closeVelocity = Velocity.makeVelocityFromSpeedDirection(1.1,1.1);//new Velocity(1.1, 1.1);

        pid.getCorrection(velocity, farVelocity,0);
        VelocityPID.Correction correction = pid.getCorrection(velocity, closeVelocity,0);

        assertTrue(correction.direction < 0);
        assertTrue(correction.speed < 0);
    }

}