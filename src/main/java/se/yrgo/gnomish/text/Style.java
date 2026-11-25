package se.yrgo.gnomish.text;

/**
 * Decides how text with a given {@link Role} looks. It lets the I/O-free engine highlight names and
 * numbers without knowing whether it is talking to a color terminal, a pipe or a test.
 *
 * <p>Only apply a style to short leaf tokens such as names, numbers and fixed phrases, never to
 * text that already contains styled parts, since a style may reset all formatting at its end.
 */
@FunctionalInterface
public interface Style {

  /** Leaves all text unchanged. Used in tests and when colors are turned off. */
  Style PLAIN = (role, text) -> text;

  /** Returns {@code text} decorated to show that it has the given role. */
  String apply(Role role, String text);

  /** Returns a style that colors text with ANSI escape codes for terminals. */
  static Style ansi() {
    return new AnsiStyle();
  }
}
