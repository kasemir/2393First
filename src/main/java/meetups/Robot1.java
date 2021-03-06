package meetups;

import robot.BasicRobot;

// Simplest robot:
// New "Robot.." class based ('superclass') on BasicRobot.
// To select, set build.properties
// robot.class=meetups.Robot1
public class Robot1 extends BasicRobot
{
    // We don't add anything to the BasicRobot,
    // but it already does quite a lot:
    // 1) Talks to the DriveStation
    // 2) Reports battery voltage
    // 3) Reacts to "Teleop", "Autonomous", "Disabled", .. modes
    //    by printing when we enter a mode
}
