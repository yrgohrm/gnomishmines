package se.yrgo.gnomish.model;

import java.util.Objects;
import java.util.Optional;

/** A hostile creature that can be fought. Monsters fight back when attacked. */
public final class Monster {

  private final String name;
  private final String description;
  private final int maxHealth;
  private final int minDamage;
  private final int maxDamage;
  private final Item loot;
  private int health;

  /**
   * Creates a monster.
   *
   * @param name the name used in commands and messages, e.g. {@code "troll"}
   * @param description shown when the player looks around
   * @param maxHealth starting health
   * @param minDamage least damage dealt per hit
   * @param maxDamage most damage dealt per hit
   * @param loot item dropped when the monster dies, or {@code null} for none
   */
  public Monster(
      String name, String description, int maxHealth, int minDamage, int maxDamage, Item loot) {
    if (maxHealth <= 0) {
      throw new IllegalArgumentException("maxHealth must be positive");
    }
    if (minDamage < 0 || maxDamage < minDamage) {
      throw new IllegalArgumentException("invalid damage range");
    }
    this.name = Objects.requireNonNull(name, "name");
    this.description = Objects.requireNonNull(description, "description");
    this.maxHealth = maxHealth;
    this.minDamage = minDamage;
    this.maxDamage = maxDamage;
    this.loot = loot;
    this.health = maxHealth;
  }

  /** Creates a monster that drops nothing. */
  public Monster(String name, String description, int maxHealth, int minDamage, int maxDamage) {
    this(name, description, maxHealth, minDamage, maxDamage, null);
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  public int health() {
    return health;
  }

  public int maxHealth() {
    return maxHealth;
  }

  public int minDamage() {
    return minDamage;
  }

  public int maxDamage() {
    return maxDamage;
  }

  public Optional<Item> loot() {
    return Optional.ofNullable(loot);
  }

  public boolean isAlive() {
    return health > 0;
  }

  /** Reduces health by {@code amount}, never going below zero. */
  public void takeDamage(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must not be negative");
    }
    health = Math.max(0, health - amount);
  }

  /** Returns true if the player's text refers to this monster. */
  public boolean matches(String query) {
    return Names.matches(name, query);
  }
}
