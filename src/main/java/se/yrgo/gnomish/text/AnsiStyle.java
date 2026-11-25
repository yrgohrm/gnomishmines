package se.yrgo.gnomish.text;

import java.util.Objects;

/** Colors text with ANSI SGR escape codes, ending each styled token with a full reset. */
final class AnsiStyle implements Style {

  private static final String ESC = "\u001B";

  @Override
  public String apply(Role role, String text) {
    Objects.requireNonNull(role, "role");
    Objects.requireNonNull(text, "text");
    return ESC + "[" + code(role) + "m" + text + ESC + "[0m";
  }

  private static String code(Role role) {
    return switch (role) {
      case PERSON -> "36";
      case ITEM -> "33";
      case TREASURE -> "1;35";
      case MONSTER -> "1;31";
      case ROOM_TITLE -> "1";
      case DAMAGE_DEALT -> "32";
      case DAMAGE_TAKEN -> "31";
      case HEALING -> "32";
      case HEALTH_GOOD -> "32";
      case HEALTH_LOW -> "33";
      case HEALTH_CRITICAL -> "1;31";
      case VICTORY -> "1;32";
      case DEFEAT -> "1;31";
      case SPEECH -> "3";
    };
  }
}
