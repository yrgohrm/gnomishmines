package se.yrgo.gnomish;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import se.yrgo.gnomish.engine.Dice;
import se.yrgo.gnomish.engine.Game;
import se.yrgo.gnomish.engine.RandomDice;
import se.yrgo.gnomish.text.Style;
import se.yrgo.gnomish.world.GnomishMines;

/**
 * Starts the game in the terminal.
 *
 * <p>Usage: {@code java -jar gnomish-mines.jar [--seed <number>] [--no-color] [--help]
 * [--version]}. A seed makes every dice roll repeatable, which is handy for demos. Output is
 * colored only in an interactive terminal, and never when {@code --no-color} is given or the {@code
 * NO_COLOR} environment variable is set.
 *
 * <p>The options are declared with picocli annotations instead of being parsed by hand. Picocli
 * validates them, generates the help text and reports mistakes with a usage message, so this class
 * only has to say which options exist.
 */
@Command(
    name = "gnomish-mines",
    mixinStandardHelpOptions = true,
    versionProvider = Main.ManifestVersion.class,
    description = "A small text adventure in the spirit of Colossal Cave.")
public final class Main implements Callable<Integer> {

  @Option(
      names = "--seed",
      paramLabel = "<number>",
      description = "Makes every dice roll repeatable, handy for demos.")
  private Long seed;

  @Option(names = "--no-color", description = "Never color the output.")
  private boolean noColor;

  private Main() {}

  public static void main(String[] args) {
    System.exit(commandLine().execute(args));
  }

  /** Creates the picocli command line for a fresh {@code Main}, ready to parse or execute. */
  static CommandLine commandLine() {
    return new CommandLine(new Main());
  }

  /** Runs the game loop. Picocli calls this after the options have been parsed successfully. */
  @Override
  public Integer call() throws IOException {
    PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    Console console = System.console();
    boolean terminal = console != null && console.isTerminal();
    Style style =
        colorEnabled(noColor(), System.getenv("NO_COLOR"), terminal) ? Style.ansi() : Style.PLAIN;
    Game game = new Game(GnomishMines.create(), dice(), style);
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
    return CommandLine.ExitCode.OK;
  }

  /** Returns seeded dice if {@code --seed} was given, otherwise unseeded random dice. */
  Dice dice() {
    Long chosenSeed = seed;
    return chosenSeed == null ? new RandomDice() : new RandomDice(chosenSeed);
  }

  /** Returns true if {@code --no-color} was given. */
  boolean noColor() {
    return noColor;
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

  /**
   * Reads the version for {@code --version} from the jar manifest, so it always matches the Maven
   * project version without being repeated in the code.
   */
  static final class ManifestVersion implements IVersionProvider {

    @Override
    public String[] getVersion() {
      String version = Main.class.getPackage().getImplementationVersion();
      return new String[] {
        version == null ? "gnomish-mines (development build)" : "gnomish-mines " + version
      };
    }
  }
}
