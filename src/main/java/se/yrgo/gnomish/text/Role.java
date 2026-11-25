package se.yrgo.gnomish.text;

/**
 * What a piece of game text means, as opposed to how it looks. The engine tags words with a role
 * and leaves the choice of colors to a {@link Style}, so game rules never depend on a terminal.
 */
public enum Role {
  /** The name of an inhabitant the player can talk to. */
  PERSON,
  /** The name of an ordinary item. */
  ITEM,
  /** The name of the treasure the player must carry out to win. */
  TREASURE,
  /** The name or description of a monster. */
  MONSTER,
  /** The name of a room, shown as its title. */
  ROOM_TITLE,
  /** Damage the player deals to a monster. */
  DAMAGE_DEALT,
  /** Damage the player takes. */
  DAMAGE_TAKEN,
  /** Health the player recovers. */
  HEALING,
  /** A health value above half of the maximum. */
  HEALTH_GOOD,
  /** A health value above a quarter of the maximum. */
  HEALTH_LOW,
  /** A health value at or below a quarter of the maximum. */
  HEALTH_CRITICAL,
  /** The winning message. */
  VICTORY,
  /** The losing message. */
  DEFEAT,
  /** A line spoken by an inhabitant. */
  SPEECH
}
