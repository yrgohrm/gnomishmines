package se.yrgo.gnomish;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import se.yrgo.gnomish.engine.Dice;
import se.yrgo.gnomish.engine.Game;
import se.yrgo.gnomish.engine.RandomDice;
import se.yrgo.gnomish.text.Style;
import se.yrgo.gnomish.world.GnomishMines;

/**
 * Starts the game in the terminal.
 *
 * <p>Usage: {@code java -jar gnomish-mines.jar [--seed <number>] [--no-color]}. A seed makes every
 * dice roll repeatable, which is handy for demos. Output is colored only in an interactive
 * terminal, and never when {@code --no-color} is given or the {@code NO_COLOR} environment variable
 * is set.
 */
public final class Main {

  private Main() {}

  public static void main(String[] args) throws IOException {
    PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    Dice dice;
    try {
      dice = diceFrom(args);
    } catch (IllegalArgumentException e) {
      new PrintStream(System.err, true, StandardCharsets.UTF_8).println(e.getMessage());
      System.exit(2);
      return;
    }
    Console console = System.console();
    boolean terminal = console != null && console.isTerminal();
    Style style =
        colorEnabled(noColorRequested(args), System.getenv("NO_COLOR"), terminal)
            ? Style.ansi()
            : Style.PLAIN;
    Game game = new Game(GnomishMines.create(), dice, style);
    BufferedReader in =
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    out.println(game.intro());
    while (!game.isOver()) {
      out.print("\n> ");
      out.flush();
      String line = in.readLine();
      if (line == null) {
        break;
      }
      out.println(game.handle(line));
    }
  }

  static final String USAGE = "Usage: gnomish-mines [--seed <number>] [--no-color]";

  private static final String NO_COLOR_FLAG = "--no-color";

  /**
   * Creates the dice from the command line arguments.
   *
   * @throws IllegalArgumentException with the usage text if the arguments are invalid
   */
  static Dice diceFrom(String[] args) {
    String[] rest =
        Arrays.stream(args).filter(arg -> !arg.equals(NO_COLOR_FLAG)).toArray(String[]::new);
    if (rest.length == 0) {
      return new RandomDice();
    }
    if (rest.length == 2 && rest[0].equals("--seed")) {
      try {
        return new RandomDice(Long.parseLong(rest[1]));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(USAGE, e);
      }
    }
    throw new IllegalArgumentException(USAGE);
  }

  /** Returns true if the command line asks for output without colors. */
  static boolean noColorRequested(String[] args) {
    return Arrays.asList(args).contains(NO_COLOR_FLAG);
  }

  /**
   * Decides whether to color the output. Kept free of I/O so the rule is easy to read and test.
   *
   * @param noColorFlag true if {@code --no-color} was given
   * @param noColorEnv the value of the {@code NO_COLOR} environment variable, or null if unset
   * @param terminal true if the game is running in an interactive terminal
   */
  static boolean colorEnabled(boolean noColorFlag, String noColorEnv, boolean terminal) {
    boolean noColorEnvSet = noColorEnv != null && !noColorEnv.isEmpty();
    return !noColorFlag && !noColorEnvSet && terminal;
  }
}
