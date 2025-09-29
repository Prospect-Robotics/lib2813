package com.team2813.lib2813.util;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix6.StatusCode;
import com.revrobotics.REVLibError;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.function.Supplier;

/**
 * Utility class for safely applying motor controller configurations (REV, Phoenix 6, CTRE).
 *
 * <p>Each configuration method retries up to a fixed number of attempts to ensure reliability,
 * reporting warnings and errors to the {@link DriverStation}.
 *
 * <p>This class is non-instantiable and provides only static methods.
 *
 * @author Team 2813
 */
public class ConfigUtils {
  private static final int ATTEMPTS = 10;

  // Make class non-instantiable
  private ConfigUtils() {
    throw new AssertionError("cannot create ConfigUtils instance");
  }

  /**
   * Applies a configuration to a REV device with retry attempts.
   *
   * @param configMethod Supplier that applies a REV configuration and returns a {@link REVLibError}.
   */
  public static void revConfig(Supplier<REVLibError> configMethod) {
    REVLibError errorCode = configMethod.get();
    for (int i = 1; i <= ATTEMPTS && errorCode != REVLibError.kOk; i++) {
      DriverStation.reportError(
          String.format("%s: Config Attempt %d Failed", errorCode.toString(), i), false);
      errorCode = configMethod.get();
    }
    if (errorCode != REVLibError.kOk) {
      DriverStation.reportError(String.format("%s: Config Failed", errorCode.toString()), false);
    }
  }

  /**
   * Applies a configuration to a Phoenix 6 device with retry attempts.
   *
   * @param configMethod Supplier that applies a Phoenix 6 configuration and returns a {@link
   *     StatusCode}.
   */
  public static void phoenix6Config(Supplier<StatusCode> configMethod) {
    StatusCode errorCode = configMethod.get();
    for (int i = 1; i <= ATTEMPTS && errorCode != StatusCode.OK; i++) {
      DriverStation.reportError(
          String.format("%s: Config Attempt %d Failed", errorCode.toString(), i), false);
      errorCode = configMethod.get();
    }
    if (errorCode != StatusCode.OK) {
      DriverStation.reportError(String.format("%s: Config Failed", errorCode.toString()), false);
    }
  }

  /**
   * Applies a configuration to a CTRE device with retry attempts.
   *
   * @param configMethod Supplier that applies a CTRE configuration and returns an {@link ErrorCode}.
   */
  public static void ctreConfig(Supplier<ErrorCode> configMethod) {
    ErrorCode errorCode = configMethod.get();
    if (errorCode != ErrorCode.OK) {
      DriverStation.reportError(
          String.format("%s: %s", "Config Attempt 1 Failed", errorCode.toString()), false);
      for (int i = 1; i < ATTEMPTS; i++) {
        errorCode = configMethod.get();
        if (errorCode == ErrorCode.OK) {
          DriverStation.reportWarning("Config Success!", false);
          return;
        } else {
          DriverStation.reportError(
              String.format(
                  "%s: %s", "Config Attempt " + (i + 1) + " Failed", errorCode.toString()),
              false);
        }
      }
      DriverStation.reportError(String.format("%s: %s", "Config Failed", errorCode), false);
    }
  }
}
