package com.team2813.lib2813.control;

/**
 * A motor with integrated PID control and encoder feedback.
 *
 * <p>Combines motor control, position sensing, and closed-loop control configuration. Some motor
 * controllers support multiple PID slots for switching between different tuning parameters.
 */
public interface PIDMotor extends Motor, Encoder {
  /**
   * Configures PIDF constants for a specific slot.
   *
   * @param slot the PID slot index (hardware-dependent, typically 0-3)
   * @param p the proportional gain
   * @param i the integral gain
   * @param d the derivative gain
   * @param f the feedforward gain
   */
  void configPIDF(int slot, double p, double i, double d, double f);

  /**
   * Configures PIDF constants for the default slot (typically slot 0).
   *
   * @param p the proportional gain
   * @param i the integral gain
   * @param d the derivative gain
   * @param f the feedforward gain
   */
  void configPIDF(double p, double i, double d, double f);

  /**
   * Configures PID constants for a specific slot with zero feedforward.
   *
   * @param slot the PID slot index (hardware-dependent, typically 0-3)
   * @param p the proportional gain
   * @param i the integral gain
   * @param d the derivative gain
   */
  void configPID(int slot, double p, double i, double d);

  /**
   * Configures PID constants for the default slot (typically slot 0) with zero feedforward.
   *
   * @param p the proportional gain
   * @param i the integral gain
   * @param d the derivative gain
   */
  void configPID(double p, double i, double d);
}
