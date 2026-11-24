package se.yrgo.gnomish.model;

import java.util.Locale;
import java.util.Optional;

/** The directions an adventurer can travel in. */
public enum Direction {
  NORTH("n"),
  SOUTH("s"),
  EAST("e"),
  WEST("w"),
  UP("u"),
  DOWN("d");

  private final String shortName;

  Direction(String shortName) {
    this.shortName = shortName;
  }

  /** Returns the lower case name used when printing exits, e.g. {@code "north"}. */
  public String displayName() {
    return name().toLowerCase(Locale.ROOT);
  }

  /**
   * Parses a direction from player input. Accepts full names ({@code "north"}) and abbreviations
   * ({@code "n"}), ignoring case.
   */
  public static Optional<Direction> parse(String text) {
    String normalized = text.trim().toLowerCase(Locale.ROOT);
    for (Direction direction : values()) {
      if (direction.displayName().equals(normalized) || direction.shortName.equals(normalized)) {
        return Optional.of(direction);
      }
    }
    return Optional.empty();
  }
}
