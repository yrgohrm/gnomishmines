package se.yrgo.gnomish.engine;

import se.yrgo.gnomish.model.Direction;

/** A parsed player command. Produced by {@link CommandParser} and executed by {@link Game}. */
public sealed interface Command {

  /** Travel in a direction. */
  record Go(Direction direction) implements Command {}

  /** Describe the current room again. */
  record Look() implements Command {}

  /** Look closely at an item, inhabitant or monster. */
  record Examine(String target) implements Command {}

  /** Pick up an item, or everything if the target is {@code "all"}. */
  record Take(String target) implements Command {}

  /** Put down an item. */
  record Drop(String target) implements Command {}

  /** List carried items. */
  record Inventory() implements Command {}

  /** Talk to an inhabitant. An empty target means "whoever is here". */
  record Talk(String target) implements Command {}

  /** Give an item to an inhabitant. */
  record Give(String item, String target) implements Command {}

  /** Ask an inhabitant on a date. */
  record Date(String target) implements Command {}

  /** Begin a romance with an inhabitant after dating them. */
  record Romance(String target) implements Command {}

  /** End the player's adventure, dismiss a romanceable inhabitant, or attack a monster. */
  record Kill(String target) implements Command {}

  /** Attack a monster. An empty target means "whatever is here". */
  record Attack(String target) implements Command {}

  /** Eat, drink or otherwise use an item. */
  record Use(String target) implements Command {}

  /** Show the player's health. */
  record Status() implements Command {}

  /** Show the list of commands. */
  record Help() implements Command {}

  /** Give up and end the game. */
  record Quit() implements Command {}

  /** Input that could not be understood. */
  record Unknown(String input) implements Command {}
}
