package robot.deepspace.grabber;

import edu.wpi.first.wpilibj.command.InstantCommand;

/** Command to extend grabber */
public class Extend extends InstantCommand
{
    private final Grabber grabber;

    public Extend(final Grabber grabber)
    {
        requires(grabber);
        this.grabber = grabber;
    }

    @Override
    protected void execute()
    {
        grabber.extend(true);
    }
}
