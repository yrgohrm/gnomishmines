package se.yrgo.gnomish.world;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import se.yrgo.gnomish.engine.Dice;
import se.yrgo.gnomish.engine.Game;
import se.yrgo.gnomish.engine.GameState;
import se.yrgo.gnomish.model.Exit;
import se.yrgo.gnomish.model.Room;
import se.yrgo.gnomish.model.World;

class GnomishMinesTest {

  /** Player always rolls the maximum, monsters always roll the minimum. A hero's luck. */
  private static final Dice LUCKY = (min, max) -> min == 1 && max == 4 ? max : min;

  @Test
  void worldHasTwelveRoomsAllReachableFromTheStart() {
    World world = GnomishMines.create();

    Set<Room> rooms = reachable(world.start(), exit -> true);

    assertThat(rooms).hasSize(12);
    assertThat(rooms).extracting(Room::name).contains("Dragon's Lair", "Hermit's Hollow");
  }

  @Test
  void entranceCanBeReachedFromEveryRoomWithoutAnyItems() {
    World world = GnomishMines.create();

    for (Room room : reachable(world.start(), exit -> true)) {
      assertThat(reachable(room, exit -> !exit.needsItem() && exit.fallDamage() == 0))
          .as("way out from %s", room.name())
          .contains(world.goal());
    }
  }

  @Test
  void createReturnsIndependentWorlds() {
    World first = GnomishMines.create();
    World second = GnomishMines.create();

    first.start().removeItem(first.start().items().getFirst());

    assertThat(second.start().items()).hasSize(2);
  }

  @Test
  void fullPlaythroughCanBeWon() {
    Game game = new Game(GnomishMines.create(), LUCKY);
    String[] commands = {
      "take all",
      "n",
      "e",
      "n",
      "talk tink",
      "s",
      "w",
      "w",
      "attack spider",
      "n",
      "take all",
      "s",
      "e",
      "n",
      "d",
      "talk morg",
      "u",
      "e",
      "attack troll",
      "attack troll",
      "e",
      "n",
      "attack dragon",
      "attack dragon",
      "attack dragon",
      "take gem",
      "s",
      "w",
      "w",
      "s",
      "s"
    };

    for (String command : commands) {
      assertThat(game.state()).as("before '%s'", command).isEqualTo(GameState.RUNNING);
      game.handle(command);
    }

    assertThat(game.state()).isEqualTo(GameState.WON);
    assertThat(game.inventoryNames()).contains("mountain gem", "rune axe", "iron sword", "rope");
  }

  @Test
  void climbingIntoTheChasmWithoutRopeIsDeadly() {
    Game game = new Game(GnomishMines.create(), LUCKY);
    game.handle("n");
    game.handle("n");

    String result = game.handle("d");

    assertThat(result).contains("Your fingers slip").contains("GAME OVER");
    assertThat(game.state()).isEqualTo(GameState.DIED);
  }

  @Test
  void slipperySlopeHurtsButIsSurvivable() {
    Game game = new Game(GnomishMines.create(), LUCKY);
    game.handle("n");

    String result = game.handle("d");

    assertThat(result).contains("Underground Lake");
    assertThat(game.playerHealth()).isEqualTo(GnomishMines.MAX_HEALTH - 3);
  }

  private static Set<Room> reachable(Room start, Predicate<Exit> usable) {
    Set<Room> seen = new LinkedHashSet<>();
    Deque<Room> queue = new ArrayDeque<>();
    queue.add(start);
    while (!queue.isEmpty()) {
      Room room = queue.removeFirst();
      if (seen.add(room)) {
        room.exitDirections().stream()
            .map(direction -> room.exit(direction).orElseThrow())
            .filter(usable)
            .map(Exit::target)
            .forEach(queue::add);
      }
    }
    return seen;
  }
}
