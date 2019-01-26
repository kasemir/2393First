package robot.deepspace;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** Drive train
 * 
 *  Each side has two motors, encoder, 2-speed gearbox
 */
public class DriveTrain extends Subsystem
{
    private final WPI_TalonSRX left = new WPI_TalonSRX(RobotMap.LEFT_MOTOR_FRONT);
    private final WPI_TalonSRX left_slave = new WPI_TalonSRX(RobotMap.LEFT_MOTOR_BACK);
    private final WPI_TalonSRX right = new WPI_TalonSRX(RobotMap.RIGHT_MOTOR_FRONT);
    private final WPI_TalonSRX right_slave = new WPI_TalonSRX(RobotMap.RIGHT_MOTOR_BACK);
    private final DifferentialDrive drive = new DifferentialDrive(left, right);
    private final Solenoid gearbox = new Solenoid(RobotMap.GEARBOX_SOLENOID);
    private double speed = 0;
    private double rotation = 0;

    public DriveTrain()
    {
        // Resets Everything To Default
        left.configFactoryDefault();
        left_slave.configFactoryDefault();
        right_slave.configFactoryDefault();
        right.configFactoryDefault();

        // TODO See if motor or sensor need to be inverted
        left.setInverted(false);
        left.setSensorPhase(true);

        left.setNeutralMode(NeutralMode.Brake);
        right.setNeutralMode(NeutralMode.Brake);

        // Use quad (relative) encoder
        left.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
        right.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
       
        // Tie Slaves To Master
        left_slave.follow(left);
        right_slave.follow(right);

        setGear(true);
    }

    @Override
    protected void initDefaultCommand() 
    {
        // Does nothing YETTTT
    }
    
    public boolean isHighGear()
    {
        return gearbox.get();
    }

    public void setGear(final boolean high)
    {
        gearbox.set(high);
        SmartDashboard.putBoolean("Gear", high);
    }
    
    public void setSpeed(final double speed)
    {
        this.speed = speed;
    }

    public void setRotation(final double new_rotation)
    {
        rotation = new_rotation;
    }

    /** Zero encoder positions */
    public void resetPosition()
    {
        left.setSelectedSensorPosition(0);
        right.setSelectedSensorPosition(0);
    }

    /** @param position Position to reach via PID */ 
    public void setPosition(final double position)
    {
        // TODO 
    }

    @Override
    public void periodic()
    {
        drive.arcadeDrive(speed, rotation, false);

        // TODO Get average with right encoder
        final int position = left.getSelectedSensorPosition();

        SmartDashboard.putNumber("Position", position);
    }
}
