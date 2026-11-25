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
  private final Optional<String> requestedGift;
  private final String dateResponse;
  private final String romanceResponse;
  private final Item gift;
  private int nextLine;
  private boolean hasTalked;
  private boolean hasGivenGift;
  private boolean hasReceivedRequestedGift;
  private boolean hasBeenDated;
  private boolean isRomanced;

  /**
   * Creates an inhabitant.
   *
   * @param name the name used in commands, e.g. {@code "Tink"}
   * @param description shown when the player looks around
   * @param dialogue the lines spoken, in order; must not be empty
   * @param gift given to the player on first conversation, or {@code null} for none
   */
  public Inhabitant(String name, String description, List<String> dialogue, Item gift) {
    this(name, description, dialogue, gift, null, "", "");
  }

  /**
   * Creates an inhabitant with a gift request and relationship responses.
   *
   * @param name the name used in commands
   * @param description shown when the player looks around
   * @param dialogue the lines spoken, in order; must not be empty
   * @param gift given to the player on first conversation, or {@code null} for none
   * @param requestedGift the item the player must give before dating, or {@code null} if none
   * @param dateResponse shown when the first date starts
   * @param romanceResponse shown when the romance starts
   */
  public Inhabitant(
      String name,
      String description,
      List<String> dialogue,
      Item gift,
      String requestedGift,
      String dateResponse,
      String romanceResponse) {
    this.name = Objects.requireNonNull(name, "name");
    this.description = Objects.requireNonNull(description, "description");
    this.dialogue = List.copyOf(dialogue);
    if (this.dialogue.isEmpty()) {
      throw new IllegalArgumentException("dialogue must not be empty");
    }
    this.gift = gift;
    this.requestedGift = Optional.ofNullable(requestedGift);
    this.dateResponse = Objects.requireNonNull(dateResponse, "dateResponse");
    this.romanceResponse = Objects.requireNonNull(romanceResponse, "romanceResponse");
    if (this.requestedGift.isPresent()
        && (requestedGift.isBlank() || dateResponse.isBlank() || romanceResponse.isBlank())) {
      throw new IllegalArgumentException("gift and relationship responses must not be blank");
    }
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
    hasTalked = true;
    String line = dialogue.get(nextLine);
    if (nextLine < dialogue.size() - 1) {
      nextLine++;
    }
    return line;
  }

  public boolean hasTalked() {
    return hasTalked;
  }

  public Optional<String> requestedGift() {
    return requestedGift;
  }

  public boolean hasReceivedRequestedGift() {
    return hasReceivedRequestedGift;
  }

  /** Records the requested gift once, returning whether the item was accepted. */
  public boolean receiveRequestedGift(String itemName) {
    Objects.requireNonNull(itemName, "itemName");
    if (hasReceivedRequestedGift
        || requestedGift.filter(name -> name.equalsIgnoreCase(itemName)).isEmpty()) {
      return false;
    }
    hasReceivedRequestedGift = true;
    return true;
  }

  /** Starts a date if the player has talked to the inhabitant and given the requested gift. */
  public boolean startDate() {
    if (!hasTalked || !hasReceivedRequestedGift || hasBeenDated) {
      return false;
    }
    hasBeenDated = true;
    return true;
  }

  public boolean hasBeenDated() {
    return hasBeenDated;
  }

  public String dateResponse() {
    return dateResponse;
  }

  /** Starts a romance after a date, returning whether this is the first romance. */
  public boolean startRomance() {
    if (!hasBeenDated || isRomanced) {
      return false;
    }
    isRomanced = true;
    return true;
  }

  public boolean isRomanced() {
    return isRomanced;
  }

  public boolean isRomanceable() {
    return requestedGift.isPresent();
  }

  public String romanceResponse() {
    return romanceResponse;
  }

  /** Hands over the gift, if any. The gift is only given once. */
  public Optional<Item> takeGift() {
    if (gift == null || hasGivenGift) {
      return Optional.empty();
    }
    hasGivenGift = true;
    return Optional.of(gift);
  }

  /** Returns the gift if it was already handed to the player. */
  public Optional<Item> giftToRecover() {
    return hasGivenGift ? Optional.ofNullable(gift) : Optional.empty();
  }

  /** Returns true if the player's text refers to this inhabitant. */
  public boolean matches(String query) {
    return Names.matches(name, query);
  }
}
