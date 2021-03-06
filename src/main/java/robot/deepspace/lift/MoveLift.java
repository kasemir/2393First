package robot.deepspace.lift;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** Command to move lift to specific position
 * 
 *  Publishes the requested position on dashboard
 *  to allow adjustments in the field.
 *  Such changes then need to be updated in the code,
 *  because they are otherwise lost when the robot
 *  is restarted.
 */
public class MoveLift extends Command
{
    private final String name;
    private final Lift lift;
    private final double height;
    private final boolean stop_pid;

    /** @param name Name used for position tweaks on dashboard
     *  @param lift Lift to use
     *  @param height Desired height
     */
    public MoveLift(final String name, final Lift lift, final double height)
    {
        this(name, lift, height, false);
    }

    /** @param name Name used for position tweaks on dashboard
     *  @param lift Lift to use
     *  @param height Desired height
     */
    public MoveLift(final String name, final Lift lift, final double height, final boolean stop_pid)
    {
        this.name = name;
        this.lift = lift;
        this.height = height;
        this.stop_pid = stop_pid;
        requires(lift);

        SmartDashboard.setDefaultNumber(name, height);
    }

    @Override
    protected void execute()
    {
        lift.setHeight(SmartDashboard.getNumber(name, height));
    }

    @Override
    protected boolean isFinished()
    {
        if (stop_pid  &&  this.timeSinceInitialized() > 5)
            return true;
        return false;
    }

    @Override
    protected void end() 
    {
        // Stop movement
        lift.drive(0);
    }
}
