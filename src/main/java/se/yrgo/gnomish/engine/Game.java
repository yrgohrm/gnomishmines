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
  private final CommandParser parser = new CommandParser();
  private GameState state = GameState.RUNNING;

  public Game(World world, Dice dice) {
    this.world = Objects.requireNonNull(world, "world");
    this.dice = Objects.requireNonNull(dice, "dice");
    this.player = new Player(world.start(), world.maxHealth());
  }

  /** Text shown when the game starts. */
  public String intro() {
    return "Welcome to the GNOMISH MINES!\n"
        + "Deep below the mountain, a dragon has stolen the gnomes' most precious treasure,\n"
        + "the "
        + world.treasure()
        + ". Find it and carry it back out of the mines.\n"
        + "Type 'help' for a list of commands.\n\n"
        + player.location().describe();
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
      case Command.Look() -> player.location().describe();
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
      return "The " + here.livingMonster().orElseThrow().name() + " blocks your way!";
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
    out.add(player.location().describe());
    if (player.location() == world.goal() && player.hasItem(world.treasure())) {
      state = GameState.WON;
      out.add(
          "\nYou step out into the daylight carrying the "
              + world.treasure()
              + ". The gnomes will sing of you for a hundred years. YOU WIN!");
    }
    return String.join("\n", out);
  }

  private String fall(int damage) {
    player.takeDamage(damage);
    return "You take " + damage + " damage from the fall. " + healthLine();
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
    return monster.description()
        + " (Health: "
        + monster.health()
        + "/"
        + monster.maxHealth()
        + ")";
  }

  private String take(String target) {
    if (target.isBlank()) {
      return "Take what?";
    }
    Room here = player.location();
    Optional<Monster> guard = here.livingMonster();
    if (guard.isPresent()) {
      return "The " + guard.get().name() + " won't let you touch anything!";
    }
    if (target.equals("all")) {
      if (here.items().isEmpty()) {
        return "There is nothing here to take.";
      }
      List<Item> taken = List.copyOf(here.items());
      taken.forEach(this::moveToInventory);
      return "Taken: " + taken.stream().map(Item::name).collect(Collectors.joining(", ")) + ".";
    }
    return here.findItem(target)
        .map(item -> {
          moveToInventory(item);
          return "Taken: " + item.name() + ".";
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
          return "Dropped: " + item.name() + ".";
        })
        .orElse("You aren't carrying any " + target + ".");
  }

  private String inventory() {
    if (player.inventory().isEmpty()) {
      return "You are empty-handed.";
    }
    return "You are carrying: "
        + player.inventory().stream().map(Item::name).collect(Collectors.joining(", "))
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
    out.append(inhabitant.name()).append(" says: \"").append(inhabitant.talk()).append('"');
    inhabitant
        .takeGift()
        .ifPresent(gift -> {
          player.addItem(gift);
          out.append("\n").append(inhabitant.name()).append(" gives you the ");
          out.append(gift.name()).append('.');
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
            + monster.name()
            + weapon.map(w -> " with your " + w.name()).orElse(" with your bare fists")
            + " for "
            + damage
            + " damage.");

    if (!monster.isAlive()) {
      out.add("The " + monster.name() + " is defeated!");
      monster
          .loot()
          .ifPresent(loot -> {
            player.location().addItem(loot);
            out.add("It dropped the " + loot.name() + ".");
          });
      return String.join("\n", out);
    }

    int counter = dice.roll(monster.minDamage(), monster.maxDamage());
    player.takeDamage(counter);
    out.add(
        "The "
            + monster.name()
            + " hits back for "
            + counter
            + " damage. "
            + healthLine()
            + " The "
            + monster.name()
            + " has "
            + monster.health()
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
      return "You will use the " + item.name() + " automatically when you fight.";
    }
    if (!item.isConsumable()) {
      return "You can't think of a way to use the " + item.name() + " right now.";
    }
    player.removeItem(item);
    int healed = player.heal(item.healing());
    return "You consume the " + item.name() + " and recover " + healed + " health. " + healthLine();
  }

  private String quit() {
    state = GameState.QUIT;
    return "You flee the mines. Perhaps another day...";
  }

  private String die(List<String> out) {
    state = GameState.DIED;
    out.add("You have died. GAME OVER.");
    return String.join("\n", out);
  }

  private String healthLine() {
    return "(Health: " + player.health() + "/" + player.maxHealth() + ")";
  }

  private static void addIfPresent(List<String> out, String message) {
    if (!message.isBlank()) {
      out.add(message);
    }
  }
}
