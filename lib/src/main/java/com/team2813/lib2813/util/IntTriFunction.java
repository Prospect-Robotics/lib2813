package com.team2813.lib2813.util;

/**
 * A functional interface that accepts three {@code int} arguments and produces a result.
 *
 * <p>This is the primitive specialization of a tri-function for {@code int} arguments.
 *
 * @param <T> the type of the result
 * @author Team 2813
 */
@FunctionalInterface
public interface IntTriFunction<T> {
  /**
   * Applies this function to the given arguments.
   *
   * @param a the first argument
   * @param b the second argument
   * @param c the third argument
   * @return the function result
   */
  T apply(int a, int b, int c);
}
