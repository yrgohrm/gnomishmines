package se.yrgo.gnomish.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.yrgo.gnomish.model.Direction;
import se.yrgo.gnomish.model.Exit;
import se.yrgo.gnomish.model.Inhabitant;
import se.yrgo.gnomish.model.Item;
import se.yrgo.gnomish.model.Monster;
import se.yrgo.gnomish.model.Room;
import se.yrgo.gnomish.model.World;

/** Tests the game rules against a small hand-built world, independent of the real map. */
class GameTest {

  private static final int MAX_HEALTH = 10;

  private Room hall;
  private Room vault;
  private Room pit;
  private Room den;

  @BeforeEach
  void buildWorld() {
    hall = new Room("Hall", "A plain hall.");
    vault = new Room("Vault", "A treasure vault.");
    pit = new Room("Pit", "The bottom of a pit.");
    den = new Room("Den", "A smelly den.");

    hall.connect(Direction.NORTH, Exit.open(vault))
        .connect(Direction.EAST, Exit.open(den))
        .connect(
            Direction.DOWN, Exit.dangerousWithout(pit, "rope", 50, "You climb down.", "You fall!"))
        .addItem(Item.plain("rope", "A rope."))
        .addItem(Item.consumable("apple", "An apple.", 4))
        .addInhabitant(
            new Inhabitant(
                "Smith",
                "A friendly smith.",
                List.of("Hello!", "Goodbye!"),
                Item.weapon("sword", "A sword.", 3)));
    vault.connect(Direction.SOUTH, Exit.open(hall)).addItem(Item.plain("gem", "A gem."));
    pit.connect(Direction.UP, Exit.requires(hall, "rope", "You climb up.", "Too steep!"));
    den.connect(Direction.WEST, Exit.open(hall))
        .connect(Direction.EAST, Exit.hazardous(pit, 3, "You slide down a chute!"))
        .setMonster(
            new Monster("wolf", "A wolf snarls.", 5, 2, 2, Item.plain("pelt", "A pelt.")),
            Direction.EAST);
  }

  private Game newGame(Dice dice) {
    return new Game(new World(hall, hall, "gem", MAX_HEALTH), dice);
  }

  private Game newGame() {
    return newGame(new ScriptedDice());
  }

  @Test
  void introDescribesStartingRoom() {
    assertThat(newGame().intro()).contains("GNOMISH MINES").contains("== Hall ==");
  }

  @Nested
  class Movement {

    @Test
    void movesThroughOpenExits() {
      Game game = newGame();

      String result = game.handle("north");

      assertThat(result).contains("== Vault ==");
      assertThat(game.currentRoomName()).isEqualTo("Vault");
    }

    @Test
    void refusesDirectionsWithoutExit() {
      Game game = newGame();

      assertThat(game.handle("west")).isEqualTo("You can't go west from here.");
      assertThat(game.currentRoomName()).isEqualTo("Hall");
    }

    @Test
    void fallingWithoutRequiredItemHurtsAndCanKill() {
      Game game = newGame();

      String result = game.handle("down");

      assertThat(result).contains("You fall!").contains("You have died");
      assertThat(game.state()).isEqualTo(GameState.DIED);
      assertThat(game.playerHealth()).isZero();
      assertThat(game.handle("look")).isEqualTo("The game is over.");
    }

    @Test
    void requiredItemMakesDangerousExitSafe() {
      Game game = newGame();
      game.handle("take rope");

      String result = game.handle("down");

      assertThat(result).contains("You climb down.").contains("== Pit ==");
      assertThat(game.playerHealth()).isEqualTo(MAX_HEALTH);
    }

    @Test
    void blockedExitNeedsItem() {
      Game game = newGame();
      game.handle("take rope");
      game.handle("down");
      game.handle("drop rope");

      assertThat(game.handle("up")).isEqualTo("Too steep!");
      assertThat(game.currentRoomName()).isEqualTo("Pit");
    }

    @Test
    void hazardousExitAlwaysHurts() {
      Game game = newGame(new ScriptedDice(4, 2, 4));
      game.handle("east");
      game.handle("attack wolf");
      game.handle("attack wolf");

      String result = game.handle("east");

      assertThat(result).contains("You slide down a chute!").contains("3 damage");
      assertThat(game.playerHealth()).isEqualTo(MAX_HEALTH - 2 - 3);
      assertThat(game.state()).isEqualTo(GameState.RUNNING);
    }

    @Test
    void livingMonsterGuardsExit() {
      Game game = newGame();
      game.handle("east");

      assertThat(game.handle("east")).isEqualTo("The wolf blocks your way!");
      assertThat(game.currentRoomName()).isEqualTo("Den");
    }
  }

  @Nested
  class Items {

    @Test
    void takeAndDropMoveItemsBetweenRoomAndInventory() {
      Game game = newGame();

      assertThat(game.handle("take rope")).isEqualTo("Taken: rope.");
      assertThat(game.inventoryNames()).containsExactly("rope");
      assertThat(game.handle("look")).doesNotContain("rope");

      assertThat(game.handle("drop rope")).isEqualTo("Dropped: rope.");
      assertThat(game.inventoryNames()).isEmpty();
      assertThat(game.handle("look")).contains("rope");
    }

    @Test
    void takeAllPicksUpEverything() {
      Game game = newGame();

      assertThat(game.handle("take all")).isEqualTo("Taken: rope, apple.");
      assertThat(game.inventoryNames()).containsExactly("rope", "apple");
      assertThat(game.handle("take all")).isEqualTo("There is nothing here to take.");
    }

    @Test
    void inventoryIsUnlimited() {
      for (int i = 0; i < 100; i++) {
        hall.addItem(Item.plain("pebble", "A pebble."));
      }
      Game game = newGame();

      game.handle("take all");

      assertThat(game.inventoryNames()).hasSize(102);
    }

    @Test
    void cannotTakeMissingItemOrDropUncarriedItem() {
      Game game = newGame();

      assertThat(game.handle("take unicorn")).isEqualTo("There is no unicorn here.");
      assertThat(game.handle("drop rope")).isEqualTo("You aren't carrying any rope.");
    }

    @Test
    void cannotTakeItemsWhileMonsterIsAlive() {
      den.addItem(Item.plain("bone", "A bone."));
      Game game = newGame();
      game.handle("east");

      assertThat(game.handle("take bone")).isEqualTo("The wolf won't let you touch anything!");
    }

    @Test
    void consumablesHealButNeverAboveMax() {
      Game game = newGame(new ScriptedDice(1, 2, 1, 2));
      game.handle("take apple");
      game.handle("east");
      game.handle("attack wolf");
      game.handle("attack wolf");

      assertThat(game.playerHealth()).isEqualTo(6);
      assertThat(game.handle("eat apple")).contains("recover 4 health");
      assertThat(game.playerHealth()).isEqualTo(10);
      assertThat(game.inventoryNames()).doesNotContain("apple");
    }

    @Test
    void healingIsCappedAtMaxHealth() {
      Game game = newGame();
      game.handle("take apple");

      assertThat(game.handle("eat apple")).contains("recover 0 health");
      assertThat(game.playerHealth()).isEqualTo(MAX_HEALTH);
    }

    @Test
    void examineShowsDescriptions() {
      Game game = newGame();

      assertThat(game.handle("examine rope")).isEqualTo("A rope.");
      assertThat(game.handle("examine smith")).isEqualTo("A friendly smith.");
      assertThat(game.handle("examine dragon")).isEqualTo("You see no dragon here.");
    }
  }

  @Nested
  class Talking {

    @Test
    void inhabitantSpeaksLinesInOrderAndRepeatsTheLast() {
      Game game = newGame();

      assertThat(game.handle("talk smith")).startsWith("Smith says: \"Hello!\"");
      assertThat(game.handle("talk smith")).isEqualTo("Smith says: \"Goodbye!\"");
      assertThat(game.handle("talk smith")).isEqualTo("Smith says: \"Goodbye!\"");
    }

    @Test
    void giftIsGivenOnlyOnce() {
      Game game = newGame();

      assertThat(game.handle("talk")).contains("Smith gives you the sword.");
      game.handle("talk");

      assertThat(game.inventoryNames()).containsExactly("sword");
    }

    @Test
    void cannotTalkToAbsentPeopleOrMonsters() {
      Game game = newGame();
      assertThat(game.handle("talk bob")).isEqualTo("There is no bob here.");

      game.handle("east");
      assertThat(game.handle("talk")).isEqualTo("There is no one here to talk to.");
      assertThat(game.handle("talk wolf")).contains("in no mood for conversation");
    }
  }

  @Nested
  class Combat {

    @Test
    void attackDamagesMonsterAndMonsterHitsBack() {
      Game game = newGame(new ScriptedDice(3, 2));
      game.handle("east");

      String result = game.handle("attack wolf");

      assertThat(result)
          .contains("with your bare fists for 3 damage")
          .contains("hits back for 2 damage")
          .contains("(Health: 8/10)")
          .contains("has 2 health left");
    }

    @Test
    void weaponAddsAttackBonus() {
      Game game = newGame(new ScriptedDice(2));
      game.handle("talk smith");
      game.handle("east");

      String result = game.handle("attack");

      assertThat(result).contains("with your sword for 5 damage").contains("defeated");
    }

    @Test
    void defeatedMonsterDropsLootAndUnblocksExit() {
      Game game = newGame(new ScriptedDice(4, 2, 4));
      game.handle("east");
      game.handle("attack wolf");

      String result = game.handle("attack wolf");

      assertThat(result).contains("The wolf is defeated!").contains("It dropped the pelt.");
      assertThat(game.handle("take pelt")).isEqualTo("Taken: pelt.");
      assertThat(game.handle("east")).contains("== Pit ==");
      assertThat(game.handle("attack")).isEqualTo("There is nothing here to fight.");
    }

    @Test
    void playerCanBeKilledInCombat() {
      den.setMonster(new Monster("ogre", "An ogre.", 100, 6, 6));
      Game game = newGame(new ScriptedDice(1, 6, 1, 6));
      game.handle("east");
      game.handle("attack ogre");

      String result = game.handle("attack ogre");

      assertThat(result).contains("You have died. GAME OVER.");
      assertThat(game.state()).isEqualTo(GameState.DIED);
    }

    @Test
    void attackingWrongTargetDoesNothing() {
      Game game = newGame();
      game.handle("east");

      assertThat(game.handle("attack dragon")).isEqualTo("There is no dragon here to fight.");
    }
  }

  @Nested
  class Ending {

    @Test
    void bringingTreasureToGoalWins() {
      Game game = newGame();
      game.handle("north");
      game.handle("take gem");

      String result = game.handle("south");

      assertThat(result).contains("YOU WIN!");
      assertThat(game.state()).isEqualTo(GameState.WON);
      assertThat(game.isOver()).isTrue();
    }

    @Test
    void quitEndsTheGame() {
      Game game = newGame();

      game.handle("quit");

      assertThat(game.state()).isEqualTo(GameState.QUIT);
    }
  }

  @Test
  void unknownCommandsGetAHelpfulAnswer() {
    Game game = newGame();

    assertThat(game.handle("dance")).contains("help");
    assertThat(game.handle("help")).contains("Commands:");
    assertThat(game.handle("health")).isEqualTo("(Health: 10/10)");
    assertThat(game.handle("i")).isEqualTo("You are empty-handed.");
  }
}
