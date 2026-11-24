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
    Game game = new Game(GnomishMines.create(), diceFrom(args));
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

  static Dice diceFrom(String[] args) {
    if (args.length == 2 && args[0].equals("--seed")) {
      return new RandomDice(Long.parseLong(args[1]));
    }
    if (args.length != 0) {
      throw new IllegalArgumentException("Usage: gnomish-mines [--seed <number>]");
    }
    return new RandomDice();
  }
}
