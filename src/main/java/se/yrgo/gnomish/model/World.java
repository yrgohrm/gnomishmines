package se.yrgo.gnomish.model;

import java.util.Objects;

/**
 * A complete game world.
 *
 * @param start the room the player starts in
 * @param goal the room the player must reach while carrying the treasure to win
 * @param treasure the name of the item the player must bring to the goal
 * @param maxHealth the player's starting and maximum health
 */
public record World(Room start, Room goal, String treasure, int maxHealth) {

  public World {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(goal, "goal");
    Objects.requireNonNull(treasure, "treasure");
    if (maxHealth <= 0) {
      throw new IllegalArgumentException("maxHealth must be positive");
    }
  }
}
