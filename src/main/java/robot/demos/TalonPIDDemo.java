package robot.demos;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import robot.BasicRobot;
import robot.parts.PDPController;

/** Demo of PID position control with TalonSRX */
public class TalonPIDDemo extends BasicRobot
{
    private final WPI_TalonSRX tal = new WPI_TalonSRX(3);

    private final Joystick joystick = new Joystick(0);

    private final PowerDistributionPanel panel = new PowerDistributionPanel(); //:)

    @Override
    public void robotInit()
    {
        super.robotInit();

        // You can also configure the Talon via the Tuner application,
        // but it's best to configure it from the code at robot initialization.
        // That way, if we later need to replace the actual Talon controller,
        // we only need to set the ID, and the rest will be configured on startup,
        // no need to do that manually in the Tuner.
        tal.configFactoryDefault();
        tal.setNeutralMode(NeutralMode.Coast); // Brake or coast?
        tal.setInverted(false);                // Invert?
        tal.configOpenloopRamp(3.0);           // Use N seconds to ramp to full speed

        // Use quad (relative) encoder
        tal.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
        // Does the sensor rotate in opposite direction compared to motor?
        tal.setSensorPhase(true);
        // Set full output range [-1, 1] or restrict it
        tal.configPeakOutputReverse(-0.5);
        tal.configPeakOutputForward(0.5);
        
        // PID Settings
        tal.configNeutralDeadband(0.04); // Default is 0.04
        tal.configClosedloopRamp(0.0);   // Use N seconds to ramp up to full PID output
        tal.config_kP(0, 0.075);         // Proportional, 
        tal.config_kI(0, 0.0005);        // Integral,
        tal.config_kD(0, 0.0);           // Differential Gain.
        tal.config_kF(0, 0.0);           // ???
        // Limit integral to about a quarter turn
        tal.config_IntegralZone(0, 1000);
        tal.configClosedLoopPeakOutput(0, 0.5);

        // Reset the sensor position
        tal.setSelectedSensorPosition(0);

        // The WPI_TalonSRX can be placed on the dashboard to show some basic info
        SmartDashboard.putData("Talon", tal);
    }

    @Override
    public void teleopPeriodic()
    {
        // Reset encoder to zero?
        if (joystick.getRawButtonPressed(PDPController.X_BUTTON))
            tal.setSelectedSensorPosition(0);
        
        // Set motor position from joystick
        // Joystick value is [-1, 1], making it positive when stick forward
        // Scale to [-4096, 4096] to get one revolution back/forward,
        // assuming encoder counts 4096 ticks per rev,
        // then scale to command -10 .. +10 turns
        final double pos = -joystick.getRawAxis(PDPController.RIGHT_STICK_VERTICAL) * 4096*10;
        tal.set(ControlMode.Position, pos);

        // If there are any faults, print them
        final Faults faults = new Faults();
        tal.getFaults(faults);
        if (faults.hasAnyFault())
            System.out.println(faults);
    
        // Display encoder position
        SmartDashboard.putNumber("Desired Position",pos);
        SmartDashboard.putNumber("Position", tal.getSelectedSensorPosition());
        SmartDashboard.putNumber("Current", panel.getTotalCurrent());
    }

    @Override
    public void autonomousPeriodic()
    {
        // Every to seconds, change from pos 0 to 1 turn (4096 encoder steps)
        if (((System.currentTimeMillis() / 2000) % 2) == 1)
            tal.set(ControlMode.Position, 0);
        else
            tal.set(ControlMode.Position, 1.0 * 4096);

        SmartDashboard.putNumber("Position", tal.getSelectedSensorPosition());
    }
}