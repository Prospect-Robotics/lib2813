package com.team2813.lib2813.feature;

import static org.junit.Assert.*;

import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FeaturesTest {
    static boolean wasEnabled;
    static final FeatureRegistry registry = FeatureRegistry.getInstance();
    static final CommandScheduler scheduler = CommandScheduler.getInstance();

    @BeforeClass
    public static void enableDriverStation() {
        wasEnabled = DriverStationSim.getEnabled();
        DriverStationSim.setEnabled(true);
        DriverStationSim.notifyNewData();
        assertTrue(DriverStationSim.getEnabled());
        assertFalse(RobotState.isDisabled());
    }

    @AfterClass
    public static void resetDriverStation() {
        if (!wasEnabled) {
            DriverStationSim.setEnabled(false);
            DriverStationSim.notifyNewData();
        }
    }

    @After
    public void resetFeatures() {
        for (FakeFeature feature : FakeFeature.values()) {
            switch (feature.featureBehavior) {
                case INITIALLY_DISABLED:
                    registry.getFeature(feature).widget.getEntry().setBoolean(false);
                    break;
                case INITIALLY_ENABLED:
                    registry.getFeature(feature).widget.getEntry().setBoolean(true);
                    break;
            }
        }
    }

    @Test
    public void whenAllEnabled_someFeaturesDisabled() {
        SimpleCommand command = new SimpleCommand();
        Command whenAllEnabled = Features.whenAllEnabled(command, FakeFeature.INITIALLY_ENABLED, FakeFeature.INITIALLY_DISABLED);
        scheduler.schedule(whenAllEnabled);
        scheduler.run();
        assertFalse(command.ran);
    }

    @Test
    public void whenAllEnabled_allFeaturesEnabled() {
        SimpleCommand command = new SimpleCommand();
        registry.getFeature(FakeFeature.INITIALLY_DISABLED).widget.getEntry().setBoolean(true);
        Command whenAllEnabled = Features.whenAllEnabled(command, FakeFeature.INITIALLY_ENABLED, FakeFeature.INITIALLY_DISABLED);
        scheduler.schedule(whenAllEnabled);
        scheduler.run();
        assertTrue(whenAllEnabled.isScheduled());
        assertTrue(command.ran);
    }

    static class SimpleCommand extends Command {
        boolean ran = false;

        @Override
        public void execute() {
            ran = true;
        }
    }

    enum FakeFeature implements FeatureIdentifier {
        ALWAYS_DISABLED(FeatureBehavior.ALWAYS_DISABLED),
        INITIALLY_DISABLED(FeatureBehavior.INITIALLY_DISABLED),
        INITIALLY_ENABLED(FeatureBehavior.INITIALLY_ENABLED);

        private final FeatureBehavior featureBehavior;

        FakeFeature(FeatureBehavior featureBehavior) {
            this.featureBehavior = featureBehavior;
        }

        @Override
        public FeatureBehavior behavior() {
            return featureBehavior;
        }
    }
}
