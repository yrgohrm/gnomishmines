package se.yrgo.gnomish.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ModelTest {

  @Nested
  class Names {

    @Test
    void matchesWholeNameOrAnySingleWordIgnoringCase() {
      Item sword = Item.weapon("Iron Sword", "A sword.", 4);

      assertThat(sword.matches("iron sword")).isTrue();
      assertThat(sword.matches("SWORD")).isTrue();
      assertThat(sword.matches("iron")).isTrue();
      assertThat(sword.matches("swo")).isFalse();
      assertThat(sword.matches("")).isFalse();
    }
  }

  @Nested
  class Players {

    private final Player player = new Player(new Room("Start", "Start."), 20);

    @Test
    void damageNeverGoesBelowZero() {
      player.takeDamage(25);

      assertThat(player.health()).isZero();
      assertThat(player.isAlive()).isFalse();
    }

    @Test
    void healReturnsAmountActuallyHealed() {
      player.takeDamage(5);

      assertThat(player.heal(10)).isEqualTo(5);
      assertThat(player.health()).isEqualTo(20);
    }

    @Test
    void bestWeaponIsTheStrongestCarried() {
      player.addItem(Item.weapon("pickaxe", "A pickaxe.", 2));
      player.addItem(Item.weapon("axe", "An axe.", 6));
      player.addItem(Item.plain("rope", "A rope."));

      assertThat(player.bestWeapon()).map(Item::name).contains("axe");
    }

    @Test
    void inventoryCannotBeModifiedFromOutside() {
      assertThatThrownBy(() -> player.inventory().add(Item.plain("x", "x")))
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  class Rooms {

    @Test
    void describeListsItemsPeopleMonsterAndExits() {
      Room room = new Room("Cave", "A dark cave.");
      room.connect(Direction.NORTH, Exit.open(new Room("Other", "Other.")))
          .connect(Direction.DOWN, Exit.open(new Room("Below", "Below.")))
          .addItem(Item.plain("rope", "A rope."))
          .addInhabitant(new Inhabitant("Pip", "A gnome.", List.of("Hi")))
          .setMonster(new Monster("bat", "A bat flutters.", 1, 0, 0), Direction.NORTH);

      assertThat(room.describe())
          .isEqualTo(
              """
              == Cave ==
              A dark cave.
              You see: rope.
              Pip is here. A gnome.
              A bat flutters. It blocks the way north!
              Exits: north, down.\
              """);
    }

    @Test
    void deadMonsterNoLongerGuards() {
      Room room = new Room("Cave", "A dark cave.");
      Monster bat = new Monster("bat", "A bat.", 1, 0, 0);
      room.setMonster(bat, Direction.NORTH);

      bat.takeDamage(1);

      assertThat(room.isGuarded(Direction.NORTH)).isFalse();
      assertThat(room.livingMonster()).isEmpty();
    }
  }

  @Test
  void invalidValuesAreRejected() {
    assertThatThrownBy(() -> new Item("x", "x", -1, 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Monster("x", "x", 0, 1, 2))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Monster("x", "x", 5, 3, 2))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Inhabitant("x", "x", List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
