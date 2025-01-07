package com.team2813.lib2813.control;

public interface PIDMotor extends Motor, Encoder {
  void configPIDF(int slot, double p, double i, double d, double f);

  void configPIDF(double p, double i, double d, double f);

  void configPID(int slot, double p, double i, double d);

  void configPID(double p, double i, double d);
}
