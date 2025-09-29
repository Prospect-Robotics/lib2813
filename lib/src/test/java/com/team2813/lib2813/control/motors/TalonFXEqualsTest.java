package com.team2813.lib2813.control.motors;

import static org.junit.Assert.assertEquals;

import com.team2813.lib2813.control.InvertType;
import org.junit.Test;

/**
 * Unit tests for {@link TalonFXWrapper}.
 *
 * <p>This class currently tests the equality behavior of a TalonFXWrapper instance.
 */
public class TalonFXEqualsTest {

  /**
   * Tests that a TalonFXWrapper instance is equal to itself.
   *
   * <p>This verifies the identity property of equals: an object must be equal to itself.
   */
  @Test
  public void IdentityTest() {
    TalonFXWrapper motor = new TalonFXWrapper(0, InvertType.CLOCKWISE);
    assertEquals("A motor should be equal to itself", motor, motor);
  }
}
