# AGENTS.md

Instructions for AI coding agents working in this repository. Humans are welcome to read along.
See `README.md` for what the project is and how to play.

## Project summary

Gnomish Mines is a small Colossal Cave style text adventure in Java 25, built with Maven. It is a
teaching and demo project for AI-assisted development, so readable, well-tested code matters more
than features or performance.

## Commands

| Task                         | Command                          |
| ---------------------------- | -------------------------------- |
| Full build (definition of done) | `mvn verify`                  |
| Run tests only               | `mvn test`                       |
| Run a single test class      | `mvn test -Dtest=GameTest`       |
| Fix formatting               | `mvn spotless:apply`             |
| Explore SpotBugs findings    | `mvn spotbugs:gui`               |
| Play the game                | `java -jar target/gnomish-mines-1.0.0-SNAPSHOT.jar [--seed N]` |

## Definition of done

A change is finished only when `mvn verify` passes. That build:

1. compiles with `-Xlint:all` and fails on any compiler warning,
2. runs all JUnit tests,
3. checks formatting with Spotless (run `mvn spotless:apply` first, never hand-format),
4. runs SpotBugs and fails on any finding.

Do not silence a SpotBugs finding by default. Fix the code. Add an entry to
`spotbugs-exclude.xml` only when the finding is a deliberate design choice, keep the match as
narrow as possible, and add an XML comment explaining why.

## Architecture

```
se.yrgo.gnomish
  Main                 Terminal entry point. The only class that reads stdin or writes stdout.
  engine
    Game               Applies commands to the game state and returns response text. No I/O.
    Command            Sealed interface of records, one per command.
    CommandParser      Turns raw input into a Command. Forgiving about case and filler words.
    Dice / RandomDice  Randomness behind an interface so tests can script rolls.
    GameState          RUNNING, WON, DIED, QUIT.
  model                Plain domain objects: Room, Exit, Item, Monster, Inhabitant, Player, World.
  world
    GnomishMines       Builds the actual map in Java code.
```

Key design rules:

- `Game` must stay free of I/O. It takes a line of text and returns a string, which keeps the
  whole game testable without a terminal.
- All randomness goes through `Dice`. Never use `Math.random()` or `new Random()` elsewhere.
- Adding a command means: a new record in `Command`, a case in `CommandParser`, a case in the
  exhaustive `switch` in `Game.handle`, a line in the help text, and tests. The compiler will
  point out the missing `switch` case.
- Rooms form a mutable graph and are shared by reference. Other mutable state (lists, inventory)
  is exposed only as unmodifiable views.
- `GnomishMines.create()` must return a fresh, independent world on every call.

## Changing the world

When you add or change rooms, exits, items, inhabitants or monsters in `GnomishMines`:

- update the room list in the `GnomishMines` class javadoc,
- keep `GnomishMinesTest.fullPlaythroughCanBeWon` passing (update its command list if the route
  changes), so the game is always winnable,
- keep `entranceCanBeReachedFromEveryRoomWithoutAnyItems` passing, so the player can never get
  stuck without a way out,
- keep the tone light and family friendly.

## Coding conventions

- Java 25. Prefer records, sealed interfaces, pattern matching `switch`, `var` where it helps
  readability, and `Optional` instead of returning `null`.
- Format with google-java-format via Spotless. Two-space indentation, 100 character lines.
- Every public type gets a short javadoc explaining *why* it exists, not just what it is.
- Validate constructor arguments with `Objects.requireNonNull` and `IllegalArgumentException`.
- Do not add dependencies without asking the user first. The project intentionally has no
  runtime dependencies.

## Testing conventions

- JUnit 6 (Jupiter) with AssertJ assertions (`assertThat`), never JUnit's `assertEquals`.
- Test names describe behaviour, e.g. `fallingWithoutRequiredItemHurtsAndCanKill`.
- Structure tests as arrange / act / assert, separated by blank lines.
- Use `ScriptedDice` (test sources) for exact combat outcomes. Game rule tests in `GameTest`
  use a small hand-built world so they don't break when the real map changes.
- Use `@Nested` classes to group related tests and `@ParameterizedTest` for input tables.

## Git

- Keep commits small and focused, with imperative subject lines ("Add lantern item").
- Never commit `target/` or IDE files. Run `mvn verify` before committing.
