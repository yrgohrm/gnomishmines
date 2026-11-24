package se.yrgo.gnomish.engine;

import java.util.ArrayDeque;
import java.util.Deque;

/** Test double that returns pre-scripted rolls in order, so combat is fully predictable. */
final class ScriptedDice implements Dice {

  private final Deque<Integer> rolls = new ArrayDeque<>();

  ScriptedDice(int... rolls) {
    for (int roll : rolls) {
      this.rolls.add(roll);
    }
  }

  @Override
  public int roll(int min, int max) {
    if (rolls.isEmpty()) {
      throw new IllegalStateException("No more scripted rolls");
    }
    int roll = rolls.removeFirst();
    if (roll < min || roll > max) {
      throw new IllegalStateException(
          "Scripted roll " + roll + " outside range " + min + ".." + max);
    }
    return roll;
  }
}
