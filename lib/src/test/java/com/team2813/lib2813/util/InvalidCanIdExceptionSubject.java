package com.team2813.lib2813.util;

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.ThrowableSubject;

/**
 * Truth Subject for making assertion about the state of an {@link InvalidCanIdException} object.
 *
 * <p>Usage:
 *
 * <pre>{@code
 *   import static com.team2813.lib2813.util.InvalidCanIdExceptionSubject.assertThat;
 *   ...
 *     @Test
 *     public void someTest() {
 *       var exception =
 *           assertThrows(InvalidCanIdException.class, () -> throw new InvalidCanIdException(-7));
 *       assertThat(exception)
 *           .hasCanId(-7)          // Check provided by InvalidCanIdExceptionSubject
 *           .hasMessageThat()...   // Further checks provided by ThrowableSubject...
 *     }
 * }</pre>
 *
 * <p>See <a href="https://truth.dev/extension">Writing your own custom subject</a> to learn about
 * creating custom Truth subjects.
 */
public final class InvalidCanIdExceptionSubject extends ThrowableSubject {
  private final InvalidCanIdException actual;

  private InvalidCanIdExceptionSubject(FailureMetadata metadata, InvalidCanIdException actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /**
   * Verifies that the subject is a non-null InvalidCanIdException with CAN Id value equal to
   * {@param candId}
   *
   * @param canId
   * @return
   */
  public InvalidCanIdExceptionSubject hasCanId(int canId) {
    if (actual == null) {
      failWithActual(simpleFact("Expected InvalidCanIdException but got null"));
    } else if (actual.getCanId() != canId) {
      failWithActual(
          fact("Expected InvalidCanIdException with canId", canId),
          fact("but failed with message", actual.getMessage()),
          fact("and with canId", actual.getCanId()));
    }
    return this;
  }

  /**
   * User defined entry point for assertion.
   *
   * <p>See class usage example for its application.
   *
   * @param actual
   * @return
   */
  public static InvalidCanIdExceptionSubject assertThat(InvalidCanIdException actual) {
    // https://truth.dev/extension.html#:~:text=For%20users%E2%80%99%20convenience%2C%20define%20a%20static%20assertThat(Employee)%20shortcut%20method%3A
    // https://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/EmployeeSubjectTest.java
    return assertAbout(InvalidCanIdExceptionSubject::new).that(actual);
  }
}
