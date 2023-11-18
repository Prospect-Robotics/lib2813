package com.team2813.lib2813.util;

import java.lang.RuntimeException;

public class InvalidCanIdException extends RuntimeException {
	private int canId;
	public InvalidCanIdException(int canId) {
		super(String.format(
			"%d is not a valid can id (a valid can id is between 0 and 62, inclusive)",
			canId));
		if (0 <= canId && canId <= 62) {
			throw new IllegalArgumentException(String.format(
				"%s is a valid can id (it is between 0 and 62, inclusive)", canId));
		}
	}
	public int getCanId() {
		return canId;
	}
}
