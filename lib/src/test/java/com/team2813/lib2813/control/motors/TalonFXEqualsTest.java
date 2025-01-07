package com.team2813.lib2813.control.motors;

import static org.junit.Assert.assertEquals;

import com.team2813.lib2813.control.InvertType;
import org.junit.Test;

public class TalonFXEqualsTest {
  @Test
  public void IdentityTest() {
    TalonFXWrapper motor = new TalonFXWrapper(0, InvertType.CLOCKWISE);
    assertEquals(motor, motor);
  }
}
