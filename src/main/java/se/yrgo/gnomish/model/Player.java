package se.yrgo.gnomish.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** The adventurer. Has a location, a limited amount of health and an unlimited inventory. */
public final class Player {

  private final int maxHealth;
  private final List<Item> inventory = new ArrayList<>();
  private int health;
  private Room location;

  public Player(Room start, int maxHealth) {
    if (maxHealth <= 0) {
      throw new IllegalArgumentException("maxHealth must be positive");
    }
    this.location = Objects.requireNonNull(start, "start");
    this.maxHealth = maxHealth;
    this.health = maxHealth;
  }

  public Room location() {
    return location;
  }

  public void moveTo(Room room) {
    location = Objects.requireNonNull(room);
  }

  public int health() {
    return health;
  }

  public int maxHealth() {
    return maxHealth;
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

  /**
   * Restores up to {@code amount} health, never going above the maximum.
   *
   * @return how much health was actually restored
   */
  public int heal(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must not be negative");
    }
    int before = health;
    health = Math.min(maxHealth, health + amount);
    return health - before;
  }

  public void addItem(Item item) {
    inventory.add(Objects.requireNonNull(item));
  }

  public boolean removeItem(Item item) {
    return inventory.remove(item);
  }

  public List<Item> inventory() {
    return Collections.unmodifiableList(inventory);
  }

  public Optional<Item> findItem(String query) {
    return inventory.stream().filter(item -> item.matches(query)).findFirst();
  }

  /** Returns true if the player carries an item with exactly this name (ignoring case). */
  public boolean hasItem(String name) {
    return inventory.stream().anyMatch(item -> item.name().equalsIgnoreCase(name));
  }

  /** Returns the weapon that deals the most damage, if the player carries any. */
  public Optional<Item> bestWeapon() {
    return inventory.stream()
        .filter(Item::isWeapon)
        .max(Comparator.comparingInt(Item::attackBonus));
  }
}
