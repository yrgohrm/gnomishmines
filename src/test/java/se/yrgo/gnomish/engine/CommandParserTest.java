package se.yrgo.gnomish.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import se.yrgo.gnomish.model.Direction;

class CommandParserTest {

  private final CommandParser parser = new CommandParser();

  @ParameterizedTest
  @CsvSource({
    "n, NORTH",
    "north, NORTH",
    "go north, NORTH",
    "GO   South, SOUTH",
    "e, EAST",
    "walk west, WEST",
    "u, UP",
    "climb up, UP",
    "d, DOWN",
    "go down, DOWN"
  })
  void parsesMovement(String input, Direction expected) {
    assertThat(parser.parse(input)).isEqualTo(new Command.Go(expected));
  }

  @Test
  void parsesLookAndExamine() {
    assertThat(parser.parse("look")).isEqualTo(new Command.Look());
    assertThat(parser.parse("look at the sword")).isEqualTo(new Command.Examine("sword"));
    assertThat(parser.parse("examine troll")).isEqualTo(new Command.Examine("troll"));
  }

  @Test
  void parsesTakingAndDropping() {
    assertThat(parser.parse("take rope")).isEqualTo(new Command.Take("rope"));
    assertThat(parser.parse("get the iron sword")).isEqualTo(new Command.Take("iron sword"));
    assertThat(parser.parse("pick up potion")).isEqualTo(new Command.Take("potion"));
    assertThat(parser.parse("take all")).isEqualTo(new Command.Take("all"));
    assertThat(parser.parse("drop bread")).isEqualTo(new Command.Drop("bread"));
  }

  @Test
  void parsesSocialAndCombatCommands() {
    assertThat(parser.parse("talk to Tink")).isEqualTo(new Command.Talk("tink"));
    assertThat(parser.parse("talk")).isEqualTo(new Command.Talk(""));
    assertThat(parser.parse("give the crystal shard to Bramble"))
        .isEqualTo(new Command.Give("crystal shard", "bramble"));
    assertThat(parser.parse("give smoked fish to Pip"))
        .isEqualTo(new Command.Give("smoked fish", "pip"));
    assertThat(parser.parse("give rope")).isEqualTo(new Command.Give("rope", ""));
    assertThat(parser.parse("date Tink")).isEqualTo(new Command.Date("tink"));
    assertThat(parser.parse("romance Tink")).isEqualTo(new Command.Romance("tink"));
    assertThat(parser.parse("attack the troll")).isEqualTo(new Command.Attack("troll"));
    assertThat(parser.parse("kill dragon")).isEqualTo(new Command.Kill("dragon"));
    assertThat(parser.parse("kill yourself")).isEqualTo(new Command.Kill("yourself"));
    assertThat(parser.parse("eat mushroom")).isEqualTo(new Command.Use("mushroom"));
    assertThat(parser.parse("drink potion")).isEqualTo(new Command.Use("potion"));
  }

  @Test
  void parsesMetaCommands() {
    assertThat(parser.parse("i")).isEqualTo(new Command.Inventory());
    assertThat(parser.parse("inventory")).isEqualTo(new Command.Inventory());
    assertThat(parser.parse("health")).isEqualTo(new Command.Status());
    assertThat(parser.parse("help")).isEqualTo(new Command.Help());
    assertThat(parser.parse("quit")).isEqualTo(new Command.Quit());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "dance", "go sideways", "xyzzy"})
  void unknownInputIsReportedAsUnknown(String input) {
    assertThat(parser.parse(input)).isInstanceOf(Command.Unknown.class);
  }
}
