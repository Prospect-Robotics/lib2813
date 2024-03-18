package com.team2813.lib2813.feature;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.team2813.lib2813.feature.FakeFeatures.FakeFeature;

public final class FeatureRegistryTest {
    private FeatureRegistry registry;

    @Rule
    public final TestName testName = new TestName();

    @Before
    public void createFeatureRegistry() {
        String shuffleboardTabName = String.format("%s.%s",
                FeatureRegistryTest.class.getSimpleName(),
                testName.getMethodName());
        registry = new FeatureRegistry(shuffleboardTabName);
    }

    @After
    public void resetFeatures() {
        FakeFeatures.reset(registry);
    }

    @Test
    public void initiallyDisabled() {
        assertFalse(registry.enabled(FakeFeature.INITIALLY_DISABLED));
    }

    @Test
    public void initiallyEnabled() {
        assertTrue(registry.enabled(FakeFeature.INITIALLY_ENABLED));
    }

    @Test
    public void alwaysDisabled() {
        FakeFeature feature = FakeFeature.ALWAYS_DISABLED;
        assertFalse(registry.enabled(feature));
        assertNull(registry.getFeature(feature).widget);
    }

    @Test
    public void enable() {
        FakeFeature feature = FakeFeature.INITIALLY_DISABLED;
        assertFalse(registry.enabled(feature));
        registry.getFeature(feature).widget.getEntry().setBoolean(true);
        assertTrue(registry.enabled(feature));
    }

    @Test
    public void disable() {
        FakeFeature feature = FakeFeature.INITIALLY_ENABLED;
        assertTrue(registry.enabled(feature));
        registry.getFeature(feature).widget.getEntry().setBoolean(false);
        assertFalse(registry.enabled(feature));
    }
}
