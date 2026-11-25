package se.yrgo.gnomish.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import se.yrgo.gnomish.model.Direction;

/**
 * Turns a line of player input into a {@link Command}. The parser is forgiving: it ignores case,
 * extra whitespace and filler words, so {@code "talk to the smith"} and {@code "TALK smith"} mean
 * the same thing.
 */
public final class CommandParser {

  private static final Set<String> FILLER_WORDS = Set.of("the", "a", "an", "to", "at", "with");

  /** Parses one line of input. Never returns {@code null}. */
  public Command parse(String input) {
    String normalized = input.trim().toLowerCase(Locale.ROOT);
    List<String> rawWords = normalized.isEmpty() ? List.of() : List.of(normalized.split("\\s+"));
    if (!rawWords.isEmpty() && rawWords.getFirst().equals("give")) {
      return parseGive(rawWords);
    }

    List<String> words = words(input);
    if (words.isEmpty()) {
      return new Command.Unknown(input);
    }

    String verb = words.getFirst();
    String rest = String.join(" ", words.subList(1, words.size()));

    Optional<Direction> bareDirection = Direction.parse(verb);
    if (bareDirection.isPresent() && words.size() == 1) {
      return new Command.Go(bareDirection.get());
    }

    return switch (verb) {
      case "go", "walk", "move", "climb" ->
          Direction.parse(rest)
              .<Command>map(Command.Go::new)
              .orElseGet(() -> new Command.Unknown(input));
      case "look", "l" -> rest.isEmpty() ? new Command.Look() : new Command.Examine(rest);
      case "examine", "x", "inspect" -> new Command.Examine(rest);
      case "take", "get", "grab" -> new Command.Take(rest);
      case "pick" -> new Command.Take(stripLeading(rest, "up"));
      case "drop" -> new Command.Drop(rest);
      case "inventory", "inv", "i" -> new Command.Inventory();
      case "talk", "speak", "ask" -> new Command.Talk(rest);
      case "date" -> new Command.Date(rest);
      case "romance" -> new Command.Romance(rest);
      case "kill" -> new Command.Kill(rest);
      case "attack", "fight", "hit" -> new Command.Attack(rest);
      case "use", "eat", "drink" -> new Command.Use(rest);
      case "health", "status", "hp" -> new Command.Status();
      case "help", "?" -> new Command.Help();
      case "quit", "exit", "q" -> new Command.Quit();
      default -> new Command.Unknown(input);
    };
  }

  private static Command parseGive(List<String> rawWords) {
    int separator = rawWords.subList(1, rawWords.size()).indexOf("to") + 1;
    if (separator == 0) {
      return new Command.Give(cleanWords(rawWords.subList(1, rawWords.size())), "");
    }
    return new Command.Give(
        cleanWords(rawWords.subList(1, separator)),
        cleanWords(rawWords.subList(separator + 1, rawWords.size())));
  }

  private static String cleanWords(List<String> words) {
    return String.join(" ", words.stream().filter(word -> !FILLER_WORDS.contains(word)).toList());
  }

  private static List<String> words(String input) {
    List<String> result = new ArrayList<>();
    for (String word : input.trim().toLowerCase(Locale.ROOT).split("\\s+")) {
      if (!word.isEmpty() && !FILLER_WORDS.contains(word)) {
        result.add(word);
      }
    }
    return result;
  }

  private static String stripLeading(String text, String word) {
    if (text.equals(word)) {
      return "";
    }
    return text.startsWith(word + " ") ? text.substring(word.length() + 1) : text;
  }
}
