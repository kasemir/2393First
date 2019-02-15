package robot.deepspace;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.command.WaitCommand;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import robot.BasicRobot;
import robot.camera.CameraHandler;
import robot.camera.CameraInfo;
import robot.camera.MarkerDetector;
import robot.deepspace.drivetrain.DriveTrain;
import robot.deepspace.drivetrain.HeadingHoldJoydrive;
import robot.deepspace.drivetrain.Joydrive;
import robot.deepspace.drivetrain.MoveToPosition;
import robot.deepspace.drivetrain.ResetDrivetrain;
import robot.deepspace.drivetrain.RotateToHeading;
import robot.deepspace.drivetrain.ToggleGear;
import robot.deepspace.grabber.CloseGrabber;
import robot.deepspace.grabber.Extend;
import robot.deepspace.grabber.Grabber;
import robot.deepspace.grabber.OpenGrabber;
import robot.deepspace.grabber.Retract;
import robot.deepspace.grabber.SetSpinnerSpeed;
import robot.deepspace.grabber.ToggleGrabber;
import robot.deepspace.grabber.WaitForCargo;
import robot.deepspace.grabber.WaitForHatch;
import robot.deepspace.lift.DriveLift;
import robot.deepspace.lift.HomeLift;
import robot.deepspace.lift.Lift;
import robot.deepspace.lift.MoveLift;
import robot.deepspace.riser.DropAll;
import robot.deepspace.riser.ResetRiser;
import robot.deepspace.riser.RiseFront;
import robot.deepspace.riser.Riser;

/** Main robot class for deep space 2019
 * 
 *  TODO Documentation: PPT for OI, vision, ..
 *
 *  TODO Prepare autonomous moves from N start positions to M initial disk placements.
 *  Maybe leave last leg of route to driver, using vision, but get them close.
 */
public class DeepspaceRobot extends BasicRobot
{
    private final PowerDistributionPanel pdp = new PowerDistributionPanel();
    
    // Components, subsystems
    private final DriveTrain drivetrain = new DriveTrain();
    private final Lift lift = new Lift();
    private CameraHandler camera;
    private Grabber grabber = new Grabber();
    private Riser riser = new Riser();

    // Commands for drivetrain
    private final Command reset_drivetrain = new ResetDrivetrain(drivetrain);
    private final Command toggle_gear = new ToggleGear(drivetrain);
    private final Joydrive joydrive = new Joydrive(drivetrain);
    private final HeadingHoldJoydrive hhdrive = new HeadingHoldJoydrive(drivetrain);
    private final Rumble rumble = new Rumble();

    // .. Lift
    private final Command home_lift = new HomeLift(lift);
    private final Command drive_lift = new DriveLift(lift);
    private final Command move_lift_hatch_low = new MoveLift("Hatch Low Pos", lift, 12+7);
    private final Command move_lift_hatch_middle = new MoveLift("Hatch Mid Pos", lift, 3*12+11);
    private final Command move_lift_hatch_high = new MoveLift("Hatch Hi Pos", lift, 6*12+3);
    private final Command move_lift_cargo_low = new MoveLift("Cargo Low Pos", lift, 2*12+3.5);
    private final Command move_lift_cargo_middle = new MoveLift("Cargo Mid Pos", lift, 4*12+7.5);
    private final Command move_lift_cargo_high = new MoveLift("Cargo Hi Pos", lift, 6*12+11.5);
    private final Command move_lift_cargo_ship = new MoveLift("Cargo Ship Pos", lift, 15.5);
    private final Command move_lift_cargo_pickup = new MoveLift("Cargo Pickup Pos", lift, 30.0);

    // .. Grabber
    private final Command toggle_grabber = new ToggleGrabber(grabber);
    private final CommandGroup get_hatch = new CommandGroup();
    private final CommandGroup release_hatch = new CommandGroup();
    private final CommandGroup get_cargo = new CommandGroup();
    private final CommandGroup deposit_cargo = new CommandGroup();

    // .. Riser
    private final Command reset_riser = new ResetRiser(riser);
    private final Command drop_all = new DropAll(riser);
    private final Command rise_front = new RiseFront(riser);

    // What to start in autonomous mode
    private final SendableChooser<Command> auto_options = new SendableChooser<>();

    @Override
    public void robotInit()
    {
        super.robotInit();

        if (CameraInfo.haveCamera())
            camera = new CameraHandler(320, 240, 10, new MarkerDetector());

        // Fill command groups =======================================

        // Get hatch panel
        // TODO Maybe start by moving lift to loading station height
        // get_hatch.addSequential(new StartCommand(move_lift_low));
        get_hatch.addSequential(new Retract(grabber));
        get_hatch.addSequential(new OpenGrabber(grabber));
        get_hatch.addSequential(new WaitForHatch(grabber));
        get_hatch.addSequential(new CloseGrabber(grabber));
        // TODO Maybe add command to lift the hatch panel
        // off the lower brush in the loading station
        // get_hatch.addSequential(new MoveLift("Loading Station Get Out", lift, 18));
        // Then drivers need to move robot away from loading station,
        // to spaceship or rocket, and push low/mid/high position buttons.

        // Release hatch panel
        release_hatch.addSequential(new Extend(grabber));
        // not sure if we should open or if it will slip off on its own
        // release_hatch.addSequential(new OpenGrabber(grabber));

        // Get Cargo
        get_cargo.addSequential(new SetSpinnerSpeed(grabber, -0.5));
        get_cargo.addSequential(new WaitForCargo(grabber));
        // Maybe we use -0.1 to make sure cargo isn't dropped
        get_cargo.addSequential(new SetSpinnerSpeed(grabber, 0));

        // Deposit Cargo
        deposit_cargo.addSequential(new SetSpinnerSpeed(grabber, 1));
        deposit_cargo.addSequential(new WaitCommand(1));
        deposit_cargo.addSequential(new SetSpinnerSpeed(grabber, 0));

        // Auto moves
        createAutoMoves();

        // Bind Buttons to commands ..
        OI.gearshift.whenPressed(toggle_gear);
        OI.togglegrabber.whenPressed(toggle_grabber);

        OI.set_lift_home.whenPressed(home_lift);
        OI.set_lift_low.whenPressed(move_lift_hatch_low);
        OI.set_lift_med.whenPressed(move_lift_hatch_middle);
        OI.set_lift_high.whenPressed(move_lift_hatch_high);

        // TODO Buttons to
        // move lift to the rocket's low/mid/high cargo openings,
        // move lift to space ship cargo opening,
        // get_hatch,
        // release_hatch,
        // drop_all, rise_front, reset_riser

        // .. and/or place them on dashboard
        SmartDashboard.putData("Auto Options", auto_options);
   
        SmartDashboard.putData("Drive", joydrive);
        SmartDashboard.putData("HH Drive", hhdrive);
        SmartDashboard.putData("Reset Drivetrain", reset_drivetrain);

        SmartDashboard.putData("Home Lift", home_lift);
        SmartDashboard.putData("Drive Lift", drive_lift);

        SmartDashboard.putData("Hatch Low", move_lift_hatch_low);
        SmartDashboard.putData("Hatch Mid", move_lift_hatch_middle);
        SmartDashboard.putData("Hatch High", move_lift_hatch_high);
        SmartDashboard.putData("Cargo Low", move_lift_cargo_low);
        SmartDashboard.putData("Cargo Mid", move_lift_cargo_middle);
        SmartDashboard.putData("Cargo High", move_lift_cargo_high);
        SmartDashboard.putData("Cargo Ship", move_lift_cargo_ship);
        SmartDashboard.putData("Cargo Pickup",move_lift_cargo_pickup);

        SmartDashboard.putData("Get Hatch", get_hatch);
        SmartDashboard.putData("Release Hatch", release_hatch);
        SmartDashboard.putData("Get Cargo", get_cargo);
        SmartDashboard.putData("Deposit Cargo", deposit_cargo);

        SmartDashboard.putData("Reset Riser", reset_riser);
        SmartDashboard.putData("Drop All", drop_all);
        SmartDashboard.putData("Rise Front", rise_front);

        // Allow "Reset" even when not in teleop or periodic
        reset_drivetrain.setRunWhenDisabled(true);
        // .. and in fact do it right now
        reset_drivetrain.start();

        // TODO Allow home_lift when disabled, and do that right now?
        // home_lift.setRunWhenDisabled(true);
        // home_lift.start();

        // TODO Make sure risers are retracted
        // reset_riser.setRunWhenDisabled(true);
        // reset_riser.start();
    }

    /** Create auto moves */
    private void createAutoMoves()
    {
        // Demo
        CommandGroup demo = new CommandGroup();
        demo.addSequential(new ResetDrivetrain(drivetrain));
        for (int i=0; i<10; ++i)
        {
            demo.addSequential(new RotateToHeading(drivetrain, 90));
            demo.addSequential(new WaitCommand(3));
            demo.addSequential(new RotateToHeading(drivetrain, 0));
            demo.addSequential(new WaitCommand(3));
        }
        auto_options.addOption("0deg 90deg", demo);

        demo = new CommandGroup();
        demo.addSequential(new ResetDrivetrain(drivetrain));
        for (int i=0; i<10; ++i)
        {
            demo.addSequential(new MoveToPosition(drivetrain, 2*12));
            demo.addSequential(new WaitCommand(3));
            demo.addSequential(new MoveToPosition(drivetrain, 0*12));
            demo.addSequential(new WaitCommand(3));
        }
        auto_options.addOption("2ft and back", demo);

        demo = new CommandGroup();
        demo.addSequential(new ResetDrivetrain(drivetrain));
        demo.addSequential(new MoveToPosition(drivetrain, 6*12, 0));
        demo.addSequential(new RotateToHeading(drivetrain, 90));
        demo.addSequential(new MoveToPosition(drivetrain, (6+3)*12, 90));
        demo.addSequential(new RotateToHeading(drivetrain, 180));
        demo.addSequential(new MoveToPosition(drivetrain, (6+3+6)*12, 180));
        demo.addSequential(new RotateToHeading(drivetrain, 270));
        demo.addSequential(new MoveToPosition(drivetrain, (6+3+6+3)*12, 270));
        demo.addSequential(new RotateToHeading(drivetrain, 360));
        auto_options.addOption("Rectangle", demo);

        // Left start position to Rocket port 1
        demo = new CommandGroup();
        demo.addSequential(new ResetDrivetrain(drivetrain));
        demo.addSequential(new MoveToPosition(drivetrain, 6*12));
        demo.addSequential(new RotateToHeading(drivetrain, -20));
        demo.addSequential(new MoveToPosition(drivetrain, (6+3)*12));
        // .. and back for testing
        demo.addSequential(new MoveToPosition(drivetrain, 6*12));
        demo.addSequential(new RotateToHeading(drivetrain, 0));
        demo.addSequential(new MoveToPosition(drivetrain, 0));
        auto_options.addOption("L to R1", demo);

        demo = new CommandGroup();
        demo.addSequential(new ResetDrivetrain(drivetrain));
        demo.addSequential(new MoveToPosition(drivetrain, 118, 0));
        demo.addSequential(new RotateToHeading(drivetrain, 90));
        demo.addSequential(new MoveToPosition(drivetrain, 202, 90));
        auto_options.addOption("Test1", demo);


        // Also allow "Nothing"
        auto_options.setDefaultOption("Nothing", new WaitCommand(0.1));
    }

    @Override
    public void robotPeriodic()
    {
        Scheduler.getInstance().run();

        if (OI.isCargoButtonPressed())
        {
            if (grabber.isCargoDetected())
                deposit_cargo.start();
            else 
                get_cargo.start();
        }

        if (OI.isHatchButtonPressed())
        {
            if (grabber.isHatchDetected())
                release_hatch.start();
            else 
                get_hatch.start();
        }

        SmartDashboard.putNumber("Current [A]", pdp.getTotalCurrent());
        SmartDashboard.putNumber("Capacity [KWh]", pdp.getTotalEnergy()/60/60/1000);
    }

    private void updateJoystickDrivemode()
    {
        // Toggle between plain drive
        // and heading-hold mode
        if (OI.isToggleHeadingholdPressed())
        {
            if (joydrive.isRunning())
            {
                joydrive.cancelbutdontstop();
                hhdrive.start();
                rumble.start(0.1);
            }
            else
            {
                hhdrive.cancelbutdontstop();
                joydrive.start();
                rumble.start(0.5);
            }
        }
    }
    
    private void handlebuttonboard()
    {
        if (OI.isPickUpPressed())
        {
            if (OI.isCargoModeEnabled())
                move_lift_cargo_pickup.start();
            else
                move_lift_hatch_low.start();
        }

        if (OI.isCargoShipPressed())
        {
            if (OI.isCargoModeEnabled())
                move_lift_cargo_ship.start();
            else
                move_lift_hatch_low.start();
        }

        if (OI.isRocketLowPressed())
        {
            if (OI.isCargoModeEnabled())
                move_lift_cargo_low.start();
            else
                move_lift_hatch_low.start();
        }

        if (OI.isRocketMedPressed())
        {
            if (OI.isCargoModeEnabled())
                move_lift_cargo_middle.start();
            else
                move_lift_hatch_middle.start();
        }

        if (OI.isRocketHighPressed())
        {
            if (OI.isCargoModeEnabled())
                move_lift_cargo_high.start();
            else
                move_lift_hatch_high.start();
        }

        if (OI.riserAllDown())
            drop_all.start();

        if (OI.riserFrontUp())
            rise_front.start();

        if (OI.riserAllUp());
            reset_riser.start();
    }

    @Override
    public void teleopInit()
    {
        super.teleopInit();

        //Reading to clear any pending button presses
        OI.isToggleHeadingholdPressed();

        // Start driving by joystick
        joydrive.start();
    }

    @Override
    public void teleopPeriodic()
    {
        // .. and allow toggling between HH mode and plain joydrive
        updateJoystickDrivemode();
        handlebuttonboard();
    }

    @Override
    public void autonomousInit()
    {
        super.autonomousInit();

        //Reading to clear any pending button presses
        OI.isToggleHeadingholdPressed();

        // Start the selected option, which may be "Nothing"
        auto_options.getSelected().start();
        // Driving by joystick is initially off
    }

    @Override
    public void autonomousPeriodic()
    {
        
        // Test drive PID
        // if ((System.currentTimeMillis() / 5000) % 2 == 1)
        //     drivetrain.setPosition(24);
        // else
        //     drivetrain.setPosition(0);


        // Pressing a button will start driving by joystick,
        // then toggle between plain and HH mode
        updateJoystickDrivemode();
        handlebuttonboard();
    }
}