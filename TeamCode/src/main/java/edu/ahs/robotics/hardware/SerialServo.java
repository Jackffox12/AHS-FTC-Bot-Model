package edu.ahs.robotics.hardware;

import com.qualcomm.robotcore.hardware.Servo;

import edu.ahs.robotics.util.ftc.FTCUtilities;

public class SerialServo {
    private Servo servo;

    public SerialServo(String deviceName, boolean reverse) {
        servo = FTCUtilities.getSerialServo(deviceName);
        if(reverse){
            servo.setDirection(Servo.Direction.REVERSE);
        } else {
            servo.setDirection(Servo.Direction.FORWARD);
        }
    }

    public void setPosition(double position){
        servo.setPosition(position);
    }

    public void mapPosition(double min, double max){
        servo.scaleRange(min, max);
    }

}
