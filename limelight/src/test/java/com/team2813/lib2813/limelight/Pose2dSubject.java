package com.team2813.lib2813.limelight;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertAbout;

/**
 * Truth Subject for making assertions about {@link Pose2d} values.
 *
 * <p>See <a href="https://truth.dev/extension">Writing your own custom subject</a>
 * to learn about creating custom Truth subjects.
 */
final class Pose2dSubject extends Pose3dSubject {

    // User-defined entry point
    public static Pose3dSubject assertThat(@Nullable Pose2d pose) {
        return assertAbout(pose2ds()).that(pose);
    }

    // Static method for getting the subject factory (for use with assertAbout())
    public static Subject.Factory<Pose2dSubject, Pose2d> pose2ds() {
        return Pose2dSubject::new;
    }

    private Pose2dSubject(FailureMetadata failureMetadata, @Nullable Pose2d subject) {
        super(failureMetadata, toPose3d(subject));
    }

    private static @Nullable Pose3d toPose3d(@Nullable Pose2d pose) {
        if (pose == null) {
            return null;
        }
        return new Pose3d(pose);
    }
}
