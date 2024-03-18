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

import java.util.Arrays;

import com.team2813.lib2813.feature.FakeFeatures.FakeFeature;

public final class FeaturesTest {
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
        FakeFeatures.reset(registry);
    }

    @Test
    public void whenAllEnabled_someFeaturesDisabled() {
        SimpleCommand command = new SimpleCommand();
        Command whenAllEnabled = Features.ifAllEnabled(() -> command, FakeFeature.INITIALLY_ENABLED, FakeFeature.INITIALLY_DISABLED);
        scheduler.schedule(whenAllEnabled);
        scheduler.run();
        assertFalse(command.ran);
    }

    @Test
    public void whenAllEnabled_allFeaturesEnabled() {
        SimpleCommand command = new SimpleCommand();
        registry.getFeature(FakeFeature.INITIALLY_DISABLED).widget.getEntry().setBoolean(true);
        Command whenAllEnabled = Features.ifAllEnabled(() -> command, FakeFeature.INITIALLY_ENABLED, FakeFeature.INITIALLY_DISABLED);
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


    @Test
    public void allEnabled() {
        FakeFeature feature1 = FakeFeature.INITIALLY_DISABLED;
        FakeFeature feature2 = FakeFeature.INITIALLY_ENABLED;
        assertFalse(Features.allEnabled(feature1, feature2));
        registry.getFeature(feature1).widget.getEntry().setBoolean(true);
        assertTrue(Features.allEnabled(feature1, feature2));
    }

    @Test
    public void asSupplier() {
        FakeFeature feature1 = FakeFeature.INITIALLY_DISABLED;
        FakeFeature feature2 = FakeFeature.INITIALLY_ENABLED;
        assertFalse(Features.asSupplier(feature1, feature2).getAsBoolean());
        registry.getFeature(feature1).widget.getEntry().setBoolean(true);
        assertTrue(Features.asSupplier(feature1, feature2).getAsBoolean());
        assertFalse(Features.asSupplier(feature1, feature2, FakeFeature.ALWAYS_DISABLED).getAsBoolean());
    }

    @Test
    public void asSupplier_nullFeature() {
        FakeFeature feature = FakeFeature.INITIALLY_ENABLED;
        assertFalse(Features.asSupplier(feature, (FakeFeature) null).getAsBoolean());
        assertFalse(Features.asSupplier(null, feature).getAsBoolean());
    }

    @Test
    public void collectionAsSupplier() {
        FakeFeature feature1 = FakeFeature.INITIALLY_DISABLED;
        FakeFeature feature2 = FakeFeature.INITIALLY_ENABLED;
        assertFalse(Features.asSupplier(Arrays.asList(feature1, feature2)).getAsBoolean());
        registry.getFeature(feature1).widget.getEntry().setBoolean(true);
        assertTrue(Features.asSupplier(Arrays.asList(feature1, feature2)).getAsBoolean());
    }

    @Test
    public void collectionAsSupplier_nullFeature() {
        FakeFeature feature = FakeFeature.INITIALLY_ENABLED;
        assertFalse(Features.asSupplier(Arrays.asList(feature, null)).getAsBoolean());
    }
}
