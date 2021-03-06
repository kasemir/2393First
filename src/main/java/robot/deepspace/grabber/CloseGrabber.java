package robot.deepspace.grabber;

import edu.wpi.first.wpilibj.command.InstantCommand;

/** Command to close grabber */
public class CloseGrabber extends InstantCommand
{
    private Grabber grabber;

    public CloseGrabber(final Grabber grabber)
    {
        requires(grabber);
        this.grabber = grabber;
    }

    @Override
    protected void execute()
    {
        grabber.open(false);
    }
}
