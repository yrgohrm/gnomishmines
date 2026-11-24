package se.yrgo.gnomish.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** A location in the cave. Rooms are connected to each other through {@link Exit exits}. */
public final class Room {

  private final String name;
  private final String description;
  private final Map<Direction, Exit> exits = new EnumMap<>(Direction.class);
  private final List<Item> items = new ArrayList<>();
  private final List<Inhabitant> inhabitants = new ArrayList<>();
  private final Set<Direction> guardedExits = EnumSet.noneOf(Direction.class);
  private Monster monster;

  public Room(String name, String description) {
    this.name = Objects.requireNonNull(name, "name");
    this.description = Objects.requireNonNull(description, "description");
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  /** Adds an exit in the given direction, replacing any existing one. */
  public Room connect(Direction direction, Exit exit) {
    exits.put(Objects.requireNonNull(direction), Objects.requireNonNull(exit));
    return this;
  }

  public Optional<Exit> exit(Direction direction) {
    return Optional.ofNullable(exits.get(direction));
  }

  /** Returns the directions that have exits, in a stable order. */
  public Set<Direction> exitDirections() {
    return Collections.unmodifiableSet(exits.keySet());
  }

  public Room addItem(Item item) {
    items.add(Objects.requireNonNull(item));
    return this;
  }

  public boolean removeItem(Item item) {
    return items.remove(item);
  }

  public List<Item> items() {
    return Collections.unmodifiableList(items);
  }

  public Optional<Item> findItem(String query) {
    return items.stream().filter(item -> item.matches(query)).findFirst();
  }

  public Room addInhabitant(Inhabitant inhabitant) {
    inhabitants.add(Objects.requireNonNull(inhabitant));
    return this;
  }

  public List<Inhabitant> inhabitants() {
    return Collections.unmodifiableList(inhabitants);
  }

  public Optional<Inhabitant> findInhabitant(String query) {
    return inhabitants.stream().filter(person -> person.matches(query)).findFirst();
  }

  /**
   * Places a monster in the room. While the monster is alive it stops the player from using the
   * {@code guarded} exits and from picking anything up.
   */
  public Room setMonster(Monster monster, Direction... guarded) {
    this.monster = Objects.requireNonNull(monster);
    guardedExits.clear();
    guardedExits.addAll(List.of(guarded));
    return this;
  }

  /** Returns the monster in this room, if there is one that is still alive. */
  public Optional<Monster> livingMonster() {
    return Optional.ofNullable(monster).filter(Monster::isAlive);
  }

  /** Returns true if a living monster blocks the exit in the given direction. */
  public boolean isGuarded(Direction direction) {
    return livingMonster().isPresent() && guardedExits.contains(direction);
  }

  /** Builds the full text shown when the player enters or looks around. */
  public String describe() {
    StringBuilder text = new StringBuilder();
    text.append("== ").append(name).append(" ==\n").append(description);
    if (!items.isEmpty()) {
      text.append("\nYou see: ")
          .append(items.stream().map(Item::name).collect(Collectors.joining(", ")))
          .append('.');
    }
    for (Inhabitant person : inhabitants) {
      text.append("\n").append(person.name()).append(" is here. ").append(person.description());
    }
    livingMonster()
        .ifPresent(m -> {
          text.append("\n").append(m.description());
          if (!guardedExits.isEmpty()) {
            text.append(" It blocks the way ")
                .append(
                    guardedExits.stream()
                        .map(Direction::displayName)
                        .collect(Collectors.joining(" and ")))
                .append('!');
          }
        });
    text.append("\nExits: ");
    if (exits.isEmpty()) {
      text.append("none.");
    } else {
      text.append(
              exits.keySet().stream().map(Direction::displayName).collect(Collectors.joining(", ")))
          .append('.');
    }
    return text.toString();
  }
}
