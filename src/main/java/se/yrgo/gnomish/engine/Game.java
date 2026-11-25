package se.yrgo.gnomish.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.yrgo.gnomish.model.Direction;
import se.yrgo.gnomish.model.Exit;
import se.yrgo.gnomish.model.Inhabitant;
import se.yrgo.gnomish.model.Item;
import se.yrgo.gnomish.model.Monster;
import se.yrgo.gnomish.model.Player;
import se.yrgo.gnomish.model.Room;
import se.yrgo.gnomish.model.World;
import se.yrgo.gnomish.text.Role;
import se.yrgo.gnomish.text.Style;

/**
 * The game engine. Feed it lines of player input with {@link #handle(String)} and it returns the
 * text to show. The engine does no I/O itself, which keeps it easy to test.
 */
public final class Game {

  /** Damage range of an unarmed blow. Weapons add their attack bonus on top. */
  static final int BASE_MIN_DAMAGE = 1;

  static final int BASE_MAX_DAMAGE = 4;

  private static final String HELP =
      """
      Commands:
        north, south, east, west, up, down (or n, s, e, w, u, d)  - travel
        look                     - describe your surroundings
        examine <thing>          - look closely at something
        take <item> / take all   - pick things up
        drop <item>              - put something down
        inventory (i)            - list what you carry
        talk <someone>           - talk to an inhabitant
        attack <monster>         - fight a monster
        use / eat / drink <item> - use an item
        health                   - show your health
        help                     - show this text
        quit                     - give up\
      """;

  private final World world;
  private final Player player;
  private final Dice dice;
  private final Style style;
  private final CommandParser parser = new CommandParser();
  private GameState state = GameState.RUNNING;

  /** Creates a game whose output is plain text without any highlighting. */
  public Game(World world, Dice dice) {
    this(world, dice, Style.PLAIN);
  }

  /** Creates a game that highlights names and numbers in its output with the given style. */
  public Game(World world, Dice dice, Style style) {
    this.world = Objects.requireNonNull(world, "world");
    this.dice = Objects.requireNonNull(dice, "dice");
    this.style = Objects.requireNonNull(style, "style");
    this.player = new Player(world.start(), world.maxHealth());
  }

  /** Text shown when the game starts. */
  public String intro() {
    return "Welcome to the GNOMISH MINES!\n"
        + "Deep below the mountain, a dragon has stolen the gnomes' most precious treasure,\n"
        + "the "
        + style.apply(Role.TREASURE, world.treasure())
        + ". Find it and carry it back out of the mines.\n"
        + "Type 'help' for a list of commands.\n\n"
        + describeRoom();
  }

  public GameState state() {
    return state;
  }

  public boolean isOver() {
    return state.isOver();
  }

  public int playerHealth() {
    return player.health();
  }

  public int playerMaxHealth() {
    return player.maxHealth();
  }

  public String currentRoomName() {
    return player.location().name();
  }

  public List<String> inventoryNames() {
    return player.inventory().stream().map(Item::name).toList();
  }

  /** Executes one line of player input and returns the response. */
  public String handle(String input) {
    if (isOver()) {
      return "The game is over.";
    }
    return switch (parser.parse(input)) {
      case Command.Go(Direction direction) -> go(direction);
      case Command.Look() -> describeRoom();
      case Command.Examine(String target) -> examine(target);
      case Command.Take(String target) -> take(target);
      case Command.Drop(String target) -> drop(target);
      case Command.Inventory() -> inventory();
      case Command.Talk(String target) -> talk(target);
      case Command.Attack(String target) -> attack(target);
      case Command.Use(String target) -> use(target);
      case Command.Status() -> healthLine();
      case Command.Help() -> HELP;
      case Command.Quit() -> quit();
      case Command.Unknown(String text) ->
          text.isBlank() ? "Say something!" : "I don't understand that. Type 'help' for commands.";
    };
  }

  private String go(Direction direction) {
    Room here = player.location();
    Optional<Exit> maybeExit = here.exit(direction);
    if (maybeExit.isEmpty()) {
      return "You can't go " + direction.displayName() + " from here.";
    }
    if (here.isGuarded(direction)) {
      return "The " + monsterName(here.livingMonster().orElseThrow()) + " blocks your way!";
    }

    Exit exit = maybeExit.get();
    boolean equipped = !exit.needsItem() || player.hasItem(exit.requiredItem());
    if (!equipped && exit.fallDamage() == 0) {
      return exit.unsafeMessage();
    }

    List<String> out = new ArrayList<>();
    player.moveTo(exit.target());
    if (equipped) {
      addIfPresent(out, exit.safeMessage());
      if (!exit.needsItem() && exit.fallDamage() > 0) {
        out.add(fall(exit.fallDamage()));
      }
    } else {
      addIfPresent(out, exit.unsafeMessage());
      out.add(fall(exit.fallDamage()));
    }

    if (!player.isAlive()) {
      return die(out);
    }
    out.add(describeRoom());
    if (player.location() == world.goal() && player.hasItem(world.treasure())) {
      state = GameState.WON;
      out.add(
          "\nYou step out into the daylight carrying the "
              + style.apply(Role.TREASURE, world.treasure())
              + ". The gnomes will sing of you for a hundred years. "
              + style.apply(Role.VICTORY, "YOU WIN!"));
    }
    return String.join("\n", out);
  }

  private String fall(int damage) {
    player.takeDamage(damage);
    return "You take "
        + style.apply(Role.DAMAGE_TAKEN, String.valueOf(damage))
        + " damage from the fall. "
        + healthLine();
  }

  private String examine(String target) {
    if (target.isBlank()) {
      return "Examine what?";
    }
    Room here = player.location();
    Optional<String> description =
        player
            .findItem(target)
            .or(() -> here.findItem(target))
            .map(Item::description)
            .or(() -> here.findInhabitant(target).map(Inhabitant::description))
            .or(() -> here.livingMonster().filter(m -> m.matches(target)).map(this::monsterInfo));
    return description.orElse("You see no " + target + " here.");
  }

  private String monsterInfo(Monster monster) {
    return style.apply(Role.MONSTER, monster.description())
        + " (Health: "
        + healthFraction(monster.health(), monster.maxHealth())
        + ")";
  }

  private String take(String target) {
    if (target.isBlank()) {
      return "Take what?";
    }
    Room here = player.location();
    Optional<Monster> guard = here.livingMonster();
    if (guard.isPresent()) {
      return "The " + monsterName(guard.get()) + " won't let you touch anything!";
    }
    if (target.equals("all")) {
      if (here.items().isEmpty()) {
        return "There is nothing here to take.";
      }
      List<Item> taken = List.copyOf(here.items());
      taken.forEach(this::moveToInventory);
      return "Taken: " + taken.stream().map(this::itemName).collect(Collectors.joining(", ")) + ".";
    }
    return here.findItem(target)
        .map(item -> {
          moveToInventory(item);
          return "Taken: " + itemName(item) + ".";
        })
        .orElse("There is no " + target + " here.");
  }

  private void moveToInventory(Item item) {
    player.location().removeItem(item);
    player.addItem(item);
  }

  private String drop(String target) {
    if (target.isBlank()) {
      return "Drop what?";
    }
    return player
        .findItem(target)
        .map(item -> {
          player.removeItem(item);
          player.location().addItem(item);
          return "Dropped: " + itemName(item) + ".";
        })
        .orElse("You aren't carrying any " + target + ".");
  }

  private String inventory() {
    if (player.inventory().isEmpty()) {
      return "You are empty-handed.";
    }
    return "You are carrying: "
        + player.inventory().stream().map(this::itemName).collect(Collectors.joining(", "))
        + ".";
  }

  private String talk(String target) {
    List<Inhabitant> people = player.location().inhabitants();
    Optional<Inhabitant> person;
    if (target.isBlank()) {
      if (people.size() != 1) {
        return people.isEmpty() ? "There is no one here to talk to." : "Talk to whom?";
      }
      person = Optional.of(people.getFirst());
    } else {
      person = player.location().findInhabitant(target);
    }
    if (person.isEmpty()) {
      return player.location().livingMonster().filter(m -> m.matches(target)).isPresent()
          ? "The " + target + " is in no mood for conversation."
          : "There is no " + target + " here.";
    }

    Inhabitant inhabitant = person.get();
    StringBuilder out = new StringBuilder();
    out.append(personName(inhabitant))
        .append(" says: \"")
        .append(style.apply(Role.SPEECH, inhabitant.talk()))
        .append('"');
    inhabitant
        .takeGift()
        .ifPresent(gift -> {
          player.addItem(gift);
          out.append("\n").append(personName(inhabitant)).append(" gives you the ");
          out.append(itemName(gift)).append('.');
        });
    return out.toString();
  }

  private String attack(String target) {
    Optional<Monster> maybeMonster = player.location().livingMonster();
    if (maybeMonster.isEmpty()) {
      return "There is nothing here to fight.";
    }
    Monster monster = maybeMonster.get();
    if (!target.isBlank() && !monster.matches(target)) {
      return "There is no " + target + " here to fight.";
    }

    List<String> out = new ArrayList<>();
    Optional<Item> weapon = player.bestWeapon();
    int damage =
        dice.roll(BASE_MIN_DAMAGE, BASE_MAX_DAMAGE) + weapon.map(Item::attackBonus).orElse(0);
    monster.takeDamage(damage);
    out.add(
        "You strike the "
            + monsterName(monster)
            + weapon.map(w -> " with your " + itemName(w)).orElse(" with your bare fists")
            + " for "
            + style.apply(Role.DAMAGE_DEALT, String.valueOf(damage))
            + " damage.");

    if (!monster.isAlive()) {
      out.add("The " + monsterName(monster) + " is defeated!");
      monster
          .loot()
          .ifPresent(loot -> {
            player.location().addItem(loot);
            out.add("It dropped the " + itemName(loot) + ".");
          });
      return String.join("\n", out);
    }

    int counter = dice.roll(monster.minDamage(), monster.maxDamage());
    player.takeDamage(counter);
    out.add(
        "The "
            + monsterName(monster)
            + " hits back for "
            + style.apply(Role.DAMAGE_TAKEN, String.valueOf(counter))
            + " damage. "
            + healthLine()
            + " The "
            + monsterName(monster)
            + " has "
            + style.apply(
                healthRole(monster.health(), monster.maxHealth()), String.valueOf(monster.health()))
            + " health left.");
    if (!player.isAlive()) {
      return die(out);
    }
    return String.join("\n", out);
  }

  private String use(String target) {
    if (target.isBlank()) {
      return "Use what?";
    }
    Optional<Item> maybeItem = player.findItem(target);
    if (maybeItem.isEmpty()) {
      return "You aren't carrying any " + target + ".";
    }
    Item item = maybeItem.get();
    if (item.isWeapon()) {
      return "You will use the " + itemName(item) + " automatically when you fight.";
    }
    if (!item.isConsumable()) {
      return "You can't think of a way to use the " + itemName(item) + " right now.";
    }
    player.removeItem(item);
    int healed = player.heal(item.healing());
    return "You consume the "
        + itemName(item)
        + " and recover "
        + style.apply(Role.HEALING, String.valueOf(healed))
        + " health. "
        + healthLine();
  }

  private String quit() {
    state = GameState.QUIT;
    return "You flee the mines. Perhaps another day...";
  }

  private String die(List<String> out) {
    state = GameState.DIED;
    out.add("You have died. " + style.apply(Role.DEFEAT, "GAME OVER."));
    return String.join("\n", out);
  }

  private String healthLine() {
    return "(Health: " + healthFraction(player.health(), player.maxHealth()) + ")";
  }

  /** Formats "health/max", colored by how much of the maximum is left. */
  private String healthFraction(int health, int max) {
    return style.apply(healthRole(health, max), health + "/" + max);
  }

  private static Role healthRole(int health, int max) {
    if (health * 2 > max) {
      return Role.HEALTH_GOOD;
    }
    if (health * 4 > max) {
      return Role.HEALTH_LOW;
    }
    return Role.HEALTH_CRITICAL;
  }

  private String describeRoom() {
    return player.location().describe(style, world.treasure());
  }

  private String itemName(Item item) {
    Role role = item.name().equalsIgnoreCase(world.treasure()) ? Role.TREASURE : Role.ITEM;
    return style.apply(role, item.name());
  }

  private String monsterName(Monster monster) {
    return style.apply(Role.MONSTER, monster.name());
  }

  private String personName(Inhabitant inhabitant) {
    return style.apply(Role.PERSON, inhabitant.name());
  }

  private static void addIfPresent(List<String> out, String message) {
    if (!message.isBlank()) {
      out.add(message);
    }
  }
}
