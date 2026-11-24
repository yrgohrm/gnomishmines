package se.yrgo.gnomish;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import se.yrgo.gnomish.engine.Dice;
import se.yrgo.gnomish.engine.Game;
import se.yrgo.gnomish.engine.RandomDice;
import se.yrgo.gnomish.world.GnomishMines;

/**
 * Starts the game in the terminal.
 *
 * <p>Usage: {@code java -jar gnomish-mines.jar [--seed <number>]}. A seed makes every dice roll
 * repeatable, which is handy for demos.
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
    Game game = new Game(GnomishMines.create(), dice);
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

  static final String USAGE = "Usage: gnomish-mines [--seed <number>]";

  /**
   * Creates the dice from the command line arguments.
   *
   * @throws IllegalArgumentException with the usage text if the arguments are invalid
   */
  static Dice diceFrom(String[] args) {
    if (args.length == 0) {
      return new RandomDice();
    }
    if (args.length == 2 && args[0].equals("--seed")) {
      try {
        return new RandomDice(Long.parseLong(args[1]));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(USAGE, e);
      }
    }
    throw new IllegalArgumentException(USAGE);
  }
}
