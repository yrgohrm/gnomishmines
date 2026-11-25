package se.yrgo.gnomish.engine;

import java.util.SplittableRandom;

/** {@link Dice} backed by a pseudo random generator. Use a seed for repeatable games. */
public final class RandomDice implements Dice {

  private final SplittableRandom random;

  public RandomDice() {
    this.random = new SplittableRandom();
  }

  public RandomDice(long seed) {
    this.random = new SplittableRandom(seed);
  }

  @Override
  public int roll(int min, int max) {
    if (max < min) {
      throw new IllegalArgumentException("max must be >= min");
    }
    return random.nextInt(min, max);
  }
}
