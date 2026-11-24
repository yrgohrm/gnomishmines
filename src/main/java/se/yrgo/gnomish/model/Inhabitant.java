package se.yrgo.gnomish.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A friendly character the player can talk to. Each time the player talks to an inhabitant they say
 * the next line of their dialogue (repeating the last one when they run out). An inhabitant may
 * also hand over a gift the first time they are spoken to.
 */
public final class Inhabitant {

  private final String name;
  private final String description;
  private final List<String> dialogue;
  private Item gift;
  private int nextLine;

  /**
   * Creates an inhabitant.
   *
   * @param name the name used in commands, e.g. {@code "Tink"}
   * @param description shown when the player looks around
   * @param dialogue the lines spoken, in order; must not be empty
   * @param gift given to the player on first conversation, or {@code null} for none
   */
  public Inhabitant(String name, String description, List<String> dialogue, Item gift) {
    this.name = Objects.requireNonNull(name, "name");
    this.description = Objects.requireNonNull(description, "description");
    this.dialogue = List.copyOf(dialogue);
    if (this.dialogue.isEmpty()) {
      throw new IllegalArgumentException("dialogue must not be empty");
    }
    this.gift = gift;
  }

  /** Creates an inhabitant without a gift. */
  public Inhabitant(String name, String description, List<String> dialogue) {
    this(name, description, dialogue, null);
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  /** Returns the next line of dialogue and advances the conversation. */
  public String talk() {
    String line = dialogue.get(nextLine);
    if (nextLine < dialogue.size() - 1) {
      nextLine++;
    }
    return line;
  }

  /** Hands over the gift, if any. The gift is only given once. */
  public Optional<Item> takeGift() {
    Optional<Item> result = Optional.ofNullable(gift);
    gift = null;
    return result;
  }

  /** Returns true if the player's text refers to this inhabitant. */
  public boolean matches(String query) {
    return Names.matches(name, query);
  }
}
