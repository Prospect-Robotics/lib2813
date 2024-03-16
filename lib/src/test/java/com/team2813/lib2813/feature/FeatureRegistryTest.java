package com.team2813.lib2813.feature;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import java.util.Arrays;

public class FeatureRegistryTest {
    static final FeatureRegistry registry = new FeatureRegistry();

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

    @Test
    public void allEnabled() {
        FakeFeature feature1 = FakeFeature.INITIALLY_DISABLED;
        FakeFeature feature2 = FakeFeature.INITIALLY_ENABLED;
        assertFalse(registry.allEnabled(feature1, feature2));
        registry.getFeature(feature1).widget.getEntry().setBoolean(true);
        assertTrue(registry.allEnabled(feature1, feature2));
    }

    @Test
    public void asSupplier() {
        FakeFeature feature1 = FakeFeature.INITIALLY_DISABLED;
        FakeFeature feature2 = FakeFeature.INITIALLY_ENABLED;
        assertFalse(registry.asSupplier(feature1, feature2).getAsBoolean());
        registry.getFeature(feature1).widget.getEntry().setBoolean(true);
        assertTrue(registry.asSupplier(feature1, feature2).getAsBoolean());
        assertFalse(registry.asSupplier(feature1, feature2, FakeFeature.ALWAYS_DISABLED).getAsBoolean());
    }

    @Test
    public void asSupplier_nullFeature() {
        FakeFeature feature = FakeFeature.INITIALLY_ENABLED;
        assertFalse(registry.asSupplier(feature, (FakeFeature) null).getAsBoolean());
        assertFalse(registry.asSupplier(null, feature).getAsBoolean());
    }

    @Test
    public void collectionAsSupplier() {
        FakeFeature feature1 = FakeFeature.INITIALLY_DISABLED;
        FakeFeature feature2 = FakeFeature.INITIALLY_ENABLED;
        assertFalse(registry.asSupplier(Arrays.asList(feature1, feature2)).getAsBoolean());
        registry.getFeature(feature1).widget.getEntry().setBoolean(true);
        assertTrue(registry.asSupplier(Arrays.asList(feature1, feature2)).getAsBoolean());
    }

    @Test
    public void collectionAsSupplier_nullFeature() {
        FakeFeature feature = FakeFeature.INITIALLY_ENABLED;
        assertFalse(registry.asSupplier(Arrays.asList(feature, null)).getAsBoolean());
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
