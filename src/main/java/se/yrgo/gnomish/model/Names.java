package se.yrgo.gnomish.model;

import java.util.Arrays;
import java.util.Locale;

/** Helper for matching what the player typed against the names of things in the game. */
final class Names {

  private Names() {}

  /**
   * Returns true if {@code query} refers to something called {@code name}. A query matches if it
   * equals the whole name or any single word of it, ignoring case. So {@code "sword"} matches
   * {@code "iron sword"}.
   */
  static boolean matches(String name, String query) {
    String n = name.toLowerCase(Locale.ROOT);
    String q = query.trim().toLowerCase(Locale.ROOT);
    if (q.isEmpty()) {
      return false;
    }
    return n.equals(q) || Arrays.asList(n.split("\\s+")).contains(q);
  }
}
