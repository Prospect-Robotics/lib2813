package com.team2813.lib2813.limelight.truth;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;

import javax.annotation.Nullable;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

/**
 * Truth Subject for making assertions about {@link Pose3d} values.
 *
 * <p>See <a href="https://truth.dev/extension">Writing your own custom subject</a>
 * to learn about creating custom Truth subjects.
 */
public class Pose3dSubject extends Subject {

    // User-defined entry point
    public static Pose3dSubject assertThat(@Nullable Pose3d pose) {
        return assertAbout(pose3ds()).that(pose);
    }

    // Static method for getting the subject factory (for use with assertAbout())
    public static Subject.Factory<Pose3dSubject, Pose3d> pose3ds() {
        return Pose3dSubject::new;
    }

    private final Pose3d actual;

    protected Pose3dSubject(FailureMetadata failureMetadata, @Nullable Pose3d subject) {
        super(failureMetadata, subject);
        this.actual = subject;
    }

    // User-defined test assertion SPI below this point

    public TolerantComparison<Pose3d> isWithin(double tolerance) {
        return new TolerantComparison<Pose3d>() {
          @Override
          public void of(Pose3d expected) {
            translation().isWithin(tolerance).of(expected.getTranslation());
            rotation().isWithin(tolerance).of(expected.getRotation());
          }
        };
    }

    // Chained subjects methods below this point

    public Translation3dSubject translation() {
        return check("getTranslation()").about(Translation3dSubject.translation3ds()).that(nonNullActualPose().getTranslation());
    }

    public Rotation3dSubject rotation() {
        return check("getRotation()").about(Rotation3dSubject.rotation3ds()).that(nonNullActualPose().getRotation());
    }

    // Helper methods below this point

    private Pose3d nonNullActualPose() {
        if (actual == null) {
            failWithActual(simpleFact("expected a non-null Pose3d"));
        }
        return actual;
    }

    /** Truth Subject for making assertions about {@link Translation3d} values. */
    public static class Translation3dSubject extends Subject {

        // User-defined entry point
        public static Translation3dSubject assertThat(@Nullable Translation3d translation) {
            return assertAbout(translation3ds()).that(translation);
        }

        // Static method for getting the subject factory (for use with assertAbout())
        public static Factory<Translation3dSubject, Translation3d> translation3ds() {
            return Translation3dSubject::new;
        }

        private final Translation3d actual;

        private Translation3dSubject(FailureMetadata failureMetadata, @Nullable Translation3d subject) {
            super(failureMetadata, subject);
            this.actual = subject;
        }

        // User-defined test assertion SPI below this point

        public TolerantComparison<Translation3d> isWithin(double tolerance) {
            return new TolerantComparison<Translation3d>() {
                @Override
                public void of(Translation3d expected) {
                    x().isWithin(tolerance).of(expected.getX());
                    y().isWithin(tolerance).of(expected.getY());
                    z().isWithin(tolerance).of(expected.getZ());
                }
            };
        }

        public void isZero() {
            if (!Translation3d.kZero.equals(actual)) {
                failWithActual(simpleFact("expected to be zero"));
            }
        }

        // Chained subjects methods below this point

        public DoubleSubject x() {
            return check("getX()").that(nonNullActual().getX());
        }

        public DoubleSubject y() {
            return check("getY()").that(nonNullActual().getY());
        }

        public DoubleSubject z() {
            return check("getZ()").that(nonNullActual().getZ());
        }

        // Helper methods below this point

        private Translation3d nonNullActual() {
            if (actual == null) {
                failWithActual(simpleFact("expected a non-null Translation3d"));
            }
            return actual;
        }
    }

    /** Truth Subject for making assertions about {@link Rotation3d} values. */
    static class Rotation3dSubject extends Subject {
        // User-defined entry point
        public static Rotation3dSubject assertThat(@Nullable Rotation3d rotation) {
            return assertAbout(rotation3ds()).that(rotation);
        }

        // Static method for getting the subject factory (for use with assertAbout())
        public static Factory<Rotation3dSubject, Rotation3d> rotation3ds() {
            return Rotation3dSubject::new;
        }

        private final Rotation3d actual;

        private Rotation3dSubject(FailureMetadata failureMetadata, @Nullable Rotation3d subject) {
            super(failureMetadata, subject);
            this.actual = subject;
        }

        // User-defined test assertion SPI below this point

        public TolerantComparison<Rotation3d> isWithin(double tolerance) {
            return new TolerantComparison<Rotation3d>() {
                @Override
                public void of(Rotation3d expected) {
                    x().isWithin(tolerance).of(expected.getX());
                    y().isWithin(tolerance).of(expected.getY());
                    z().isWithin(tolerance).of(expected.getZ());
                }
            };
        }

        public void isZero() {
            if (!Rotation3d.kZero.equals(actual)) {
                failWithActual(simpleFact("expected to be zero"));
            }
        }

        // Chained subjects methods below this point

        public DoubleSubject x() {
            return check("getX()").that(nonNullActual().getX());
        }

        public DoubleSubject y() {
            return check("getY()").that(nonNullActual().getY());
        }

        public DoubleSubject z() {
            return check("getZ()").that(nonNullActual().getZ());
        }

        // Helper methods below this point

        private Rotation3d nonNullActual() {
            if (actual == null) {
                failWithActual(simpleFact("expected a non-null Rotation3d"));
            }
            return actual;
        }
    }

    /**
     * A partially specified check about an approximate relationship to a {@code double} subject using
     * a tolerance.
     */
    public interface TolerantComparison<T> {

        /**
         * Fails if the subject was expected to be within the tolerance of the given value but was not.
         * The subject and tolerance are specified earlier in the fluent call chain.
         */
        void of(T expected);
    }
}
