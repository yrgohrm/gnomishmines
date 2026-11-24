package se.yrgo.gnomish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.yrgo.gnomish.engine.Dice;

class MainTest {

  @Test
  void noArgumentsGivesRandomDice() {
    assertThat(Main.diceFrom(new String[0])).isNotNull();
  }

  @Test
  void sameSeedGivesSameRolls() {
    Dice first = Main.diceFrom(new String[] {"--seed", "42"});
    Dice second = Main.diceFrom(new String[] {"--seed", "42"});

    for (int i = 0; i < 20; i++) {
      assertThat(first.roll(1, 100)).isEqualTo(second.roll(1, 100));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "", "4.2", "99999999999999999999"})
  void invalidSeedGivesUsageMessage(String seed) {
    assertThatThrownBy(() -> Main.diceFrom(new String[] {"--seed", seed}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(Main.USAGE);
  }

  @Test
  void unknownArgumentsGiveUsageMessage() {
    assertThatThrownBy(() -> Main.diceFrom(new String[] {"--speed", "42"}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(Main.USAGE);
    assertThatThrownBy(() -> Main.diceFrom(new String[] {"--seed"}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(Main.USAGE);
  }
}
