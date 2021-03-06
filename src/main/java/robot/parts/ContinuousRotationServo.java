package robot.parts;

import edu.wpi.first.wpilibj.PWMSpeedController;

/** Continuous rotation servo (pot detatched from gears)
 *  that acts like a speed controller
 */
public class ContinuousRotationServo extends PWMSpeedController
{
    public ContinuousRotationServo(final int channel)
    {
        super(channel);

        // Values from Servo and Spark class
        setBounds(2.2, 1.52, 1.50, 1.48, 0.8);
        setPeriodMultiplier(PeriodMultiplier.k4X);
        setSpeed(0.0);
        setZeroLatch();
        setName("ContinuousRotationServo", getChannel());
        setInverted(true);
    }
}
