package com.team2813.lib2813.control.encoders;

import com.ctre.phoenix6.hardware.CANcoder;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.util.ConfigUtils;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * A wrapper class for CTRE's CANcoder device that implements the Encoder interface.
 *
 * <p>This wrapper provides both legacy (deprecated) methods using raw double values and modern
 * methods using WPILib's unit-safe measurement system.
 */
public class CancoderWrapper implements Encoder {
  // The actual CANcoder device object from CTRE's Phoenix 6 library
  private CANcoder cancoder;
  // Stores information about the device's ID and CAN bus
  private DeviceInformation info;

  /**
   * Creates a new CANcoder wrapper with a specified ID and CAN bus.
   *
   * @param id The device ID of the CANcoder on the CAN bus
   * @param canbus The name of the CAN bus the device is connected to
   */
  public CancoderWrapper(int id, String canbus) {
    cancoder = new CANcoder(id, canbus);
    info = new DeviceInformation(id, canbus);
  }

  /**
   * Creates a new CANcoder wrapper with just a device ID. Uses the default CAN bus.
   *
   * @param id The device ID of the CANcoder on the CAN bus
   */
  public CancoderWrapper(int id) {
    cancoder = new CANcoder(id);
    info = new DeviceInformation(id);
  }

  /**
   * Gets the current position of the encoder in rotations.
   *
   * @deprecated Use {@link #getPositionMeasure()} instead for type-safe units
   * @return The position in rotations as a double
   */
  @Deprecated
  @Override
  public double position() {
    return cancoder.getPosition().getValueAsDouble();
  }

  /**
   * Gets the current position of the encoder using WPILib's unit-safe Angle measure.
   *
   * @return The position as an Angle object in rotations
   */
  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(cancoder.getPosition().getValueAsDouble());
  }

  /**
   * Sets the current position of the encoder to a specified value. Useful for zeroing or
   * calibrating the encoder.
   *
   * @deprecated Use {@link #setPosition(Angle)} instead for type-safe units
   * @param position The position to set in rotations
   */
  @Deprecated
  @Override
  public void setPosition(double position) {
    ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position));
  }

  /**
   * Sets the current position of the encoder using WPILib's unit-safe Angle measure.
   *
   * @param position The position to set as an Angle object
   */
  @Override
  public void setPosition(Angle position) {
    ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position.in(Units.Rotations)));
  }

  /**
   * Gets direct access to the underlying CANcoder object. Use this if you need access to advanced
   * features not exposed by this wrapper.
   *
   * @return The underlying CANcoder object
   */
  public CANcoder encoder() {
    return cancoder;
  }

  /**
   * Gets the current velocity of the encoder in rotations per second.
   *
   * @deprecated Use {@link #getVelocityMeasure()} instead for type-safe units
   * @return The velocity in rotations per second as a double
   */
  @Deprecated
  @Override
  public double getVelocity() {
    return cancoder.getVelocity().getValueAsDouble();
  }

  /**
   * Gets the current velocity of the encoder using WPILib's unit-safe AngularVelocity measure.
   *
   * @return The velocity as an AngularVelocity object
   */
  @Override
  public AngularVelocity getVelocityMeasure() {
    return cancoder.getVelocity().getValue();
  }

  /**
   * Compares this CancoderWrapper with another object for equality. Two CancoderWrappers are
   * considered equal if they have the same device information.
   *
   * @param obj The object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CancoderWrapper)) return false;
    CancoderWrapper other = (CancoderWrapper) obj;
    return info.equals(other.info);
  }
}
