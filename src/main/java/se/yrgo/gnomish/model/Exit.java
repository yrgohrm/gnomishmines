package se.yrgo.gnomish.model;

import java.util.Objects;

/**
 * A way out of a room. Most exits are simply open, but some are dangerous or need an item:
 *
 * <ul>
 *   <li>{@link #open(Room)} - always passable.
 *   <li>{@link #hazardous(Room, int, String)} - always passable, but the player takes fall damage.
 *   <li>{@link #requires(Room, String, String, String)} - blocked unless the player carries an
 *       item.
 *   <li>{@link #dangerousWithout(Room, String, int, String, String)} - passable either way, but
 *       without the item the player falls and takes damage.
 * </ul>
 *
 * @param target the room the exit leads to
 * @param requiredItem name of an item needed to pass safely, or empty if none
 * @param fallDamage damage taken when falling, or 0 if the exit cannot be fallen through
 * @param safeMessage shown when passing with the required item (or always, if none is required)
 * @param unsafeMessage shown when trying to pass without the required item
 */
public record Exit(
    Room target, String requiredItem, int fallDamage, String safeMessage, String unsafeMessage) {

  public Exit {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(requiredItem, "requiredItem");
    Objects.requireNonNull(safeMessage, "safeMessage");
    Objects.requireNonNull(unsafeMessage, "unsafeMessage");
    if (fallDamage < 0) {
      throw new IllegalArgumentException("fallDamage must not be negative");
    }
  }

  /** An ordinary passage. */
  public static Exit open(Room target) {
    return new Exit(target, "", 0, "", "");
  }

  /** A passage that always hurts, like a slippery slope or a leap from a bridge. */
  public static Exit hazardous(Room target, int fallDamage, String message) {
    return new Exit(target, "", fallDamage, message, "");
  }

  /** A passage that cannot be used at all without {@code item}. */
  public static Exit requires(
      Room target, String item, String withItemMessage, String blockedMessage) {
    return new Exit(target, item, 0, withItemMessage, blockedMessage);
  }

  /** A passage that is safe with {@code item}, but causes a fall without it. */
  public static Exit dangerousWithout(
      Room target, String item, int fallDamage, String withItemMessage, String fallMessage) {
    return new Exit(target, item, fallDamage, withItemMessage, fallMessage);
  }

  public boolean needsItem() {
    return !requiredItem.isEmpty();
  }
}
