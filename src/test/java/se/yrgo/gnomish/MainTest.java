package se.yrgo.gnomish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;
import se.yrgo.gnomish.engine.Dice;

class MainTest {

  /**
   * Runs {@code Main}'s command line against string writers instead of the console. Only used with
   * arguments that never start the game, because a successful run reads from {@code System.in}.
   */
  private static final class Run {
    final CommandLine cmd = Main.commandLine();
    final StringWriter out = new StringWriter();
    final StringWriter err = new StringWriter();

    Run() {
      cmd.setOut(new PrintWriter(out, true));
      cmd.setErr(new PrintWriter(err, true));
      cmd.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.OFF));
    }

    int execute(String... args) {
      return cmd.execute(args);
    }

    Main parse(String... args) {
      cmd.parseArgs(args);
      return cmd.getCommand();
    }
  }

  @Nested
  class ValidArguments {

    @Test
    void noArgumentsGivesDiceAndAllowsColor() {
      Main main = new Run().parse();

      assertThat(main.dice()).isNotNull();
      assertThat(main.noColor()).isFalse();
    }

    @Test
    void sameSeedGivesSameRolls() {
      Dice first = new Run().parse("--seed", "42").dice();
      Dice second = new Run().parse("--seed", "42").dice();

      for (int i = 0; i < 20; i++) {
        assertThat(first.roll(1, 100)).isEqualTo(second.roll(1, 100));
      }
    }

    @Test
    void seedCanBeGivenWithEqualsSign() {
      Dice separate = new Run().parse("--seed", "42").dice();
      Dice joined = new Run().parse("--seed=42").dice();

      for (int i = 0; i < 20; i++) {
        assertThat(joined.roll(1, 100)).isEqualTo(separate.roll(1, 100));
      }
    }

    @Test
    void negativeSeedIsAccepted() {
      Dice first = new Run().parse("--seed", "-5").dice();
      Dice second = new Run().parse("--seed", "-5").dice();

      for (int i = 0; i < 20; i++) {
        assertThat(first.roll(1, 100)).isEqualTo(second.roll(1, 100));
      }
    }

    @Test
    void noColorFlagTurnsOffColor() {
      Main main = new Run().parse("--no-color");

      assertThat(main.noColor()).isTrue();
    }

    @Test
    void optionsCanComeInAnyOrder() {
      Main main = new Run().parse("--no-color", "--seed", "7");
      Dice expected = new Run().parse("--seed", "7").dice();

      assertThat(main.noColor()).isTrue();
      Dice actual = main.dice();
      for (int i = 0; i < 20; i++) {
        assertThat(actual.roll(1, 100)).isEqualTo(expected.roll(1, 100));
      }
    }
  }

  @Nested
  class InvalidArguments {

    static Stream<Arguments> invalidArguments() {
      return Stream.of(
          arguments((Object) new String[] {"--seed", "abc"}),
          arguments((Object) new String[] {"--seed", ""}),
          arguments((Object) new String[] {"--seed", "4.2"}),
          arguments((Object) new String[] {"--seed", "99999999999999999999"}),
          arguments((Object) new String[] {"--seed"}),
          arguments((Object) new String[] {"--seed", "--no-color"}),
          arguments((Object) new String[] {"--seed", "1", "--seed", "2"}),
          arguments((Object) new String[] {"--speed", "42"}),
          arguments((Object) new String[] {"extra"}));
    }

    @ParameterizedTest
    @MethodSource("invalidArguments")
    void invalidArgumentsPrintUsageAndExitWithTwo(String[] args) {
      Run run = new Run();

      int exitCode = run.execute(args);

      assertThat(exitCode).isEqualTo(CommandLine.ExitCode.USAGE).isEqualTo(2);
      assertThat(run.err.toString()).contains("Usage: gnomish-mines");
      assertThat(run.out.toString()).isEmpty();
    }
  }

  @Nested
  class HelpAndVersion {

    @ParameterizedTest
    @ValueSource(strings = {"--help", "-h"})
    void helpListsTheOptionsAndExitsWithZero(String option) {
      Run run = new Run();

      int exitCode = run.execute(option);

      assertThat(exitCode).isZero();
      assertThat(run.out.toString())
          .contains("Usage: gnomish-mines")
          .contains("--seed")
          .contains("--no-color");
    }

    @Test
    void versionPrintsTheGameNameAndExitsWithZero() {
      Run run = new Run();

      int exitCode = run.execute("--version");

      assertThat(exitCode).isZero();
      assertThat(run.out.toString()).startsWith("gnomish-mines");
    }
  }

  @Nested
  class ColorRule {

    @ParameterizedTest
    @CsvSource(
        nullValues = "null",
        value = {
          "false, null, true,  true",
          "false, '',   true,  true",
          "false, 1,    true,  false",
          "true,  null, true,  false",
          "false, null, false, false",
          "true,  1,    false, false",
        })
    void colorIsUsedOnlyInATerminalWithoutOptOut(
        boolean noColorFlag, String noColorEnv, boolean terminal, boolean expected) {
      boolean enabled = Main.colorEnabled(noColorFlag, noColorEnv, terminal);

      assertThat(enabled).isEqualTo(expected);
    }
  }
}
