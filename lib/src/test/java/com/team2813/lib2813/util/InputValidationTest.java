package com.team2813.lib2813.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class InputValidationTest {
	@Test
	public void invalidCanID() {
		int canId = -1;
		InvalidCanIdException expected = new InvalidCanIdException(canId);
		InvalidCanIdException actual = assertThrows(
				InvalidCanIdException.class,
				() -> InputValidation.checkCanId(canId));
		assertEquals(expected.getMessage(), actual.getMessage(), "Messages were not identical");
	}
}
