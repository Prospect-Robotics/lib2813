package com.team2813.lib2813.feature;

class FakeFeatures {

    public static void reset(FeatureRegistry registry) {
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

    public enum FakeFeature implements FeatureIdentifier {
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
