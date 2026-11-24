package se.yrgo.gnomish.engine;

/** Source of randomness for the game. Kept behind an interface so tests can supply fixed rolls. */
@FunctionalInterface
public interface Dice {

  /** Returns a number between {@code min} and {@code max}, both inclusive. */
  int roll(int min, int max);
}
