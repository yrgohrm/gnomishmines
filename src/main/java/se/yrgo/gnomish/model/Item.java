package se.yrgo.gnomish.model;

import java.util.Objects;

/**
 * Something that can be picked up and carried.
 *
 * @param name the short name used in commands, e.g. {@code "iron sword"}
 * @param description the text shown when the item is examined
 * @param attackBonus extra damage dealt when fighting with this item (0 if not a weapon)
 * @param healing health restored when the item is used (0 if not consumable)
 * @param useEffect the item's special use behavior, if any
 */
public record Item(
    String name, String description, int attackBonus, int healing, UseEffect useEffect) {

  public Item {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(useEffect, "useEffect");
    if (attackBonus < 0 || healing < 0) {
      throw new IllegalArgumentException("attackBonus and healing must not be negative");
    }
  }

  public Item(String name, String description, int attackBonus, int healing) {
    this(name, description, attackBonus, healing, UseEffect.NONE);
  }

  /** Special actions an item can trigger when used. */
  public enum UseEffect {
    NONE,
    FIRE_TROLL
  }

  /** Creates an item with no special use, such as a treasure or a tool. */
  public static Item plain(String name, String description) {
    return new Item(name, description, 0, 0);
  }

  /** Creates a weapon that adds {@code attackBonus} damage in combat. */
  public static Item weapon(String name, String description, int attackBonus) {
    return new Item(name, description, attackBonus, 0);
  }

  /** Creates food or medicine that restores {@code healing} health when used. */
  public static Item consumable(String name, String description, int healing) {
    return new Item(name, description, 0, healing);
  }

  /** Creates the one-use gun that summons a troll when fired. */
  public static Item trollGun() {
    return new Item(
        "troll gun",
        "A clunky contraption that fires a small troll-shaped cannonball.",
        0,
        0,
        UseEffect.FIRE_TROLL);
  }

  public boolean isWeapon() {
    return attackBonus > 0;
  }

  public boolean isConsumable() {
    return healing > 0;
  }

  public boolean firesTroll() {
    return useEffect == UseEffect.FIRE_TROLL;
  }

  /** Returns true if the player's text refers to this item. */
  public boolean matches(String query) {
    return Names.matches(name, query);
  }
}
