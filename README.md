# Gnomish Mines

A small text adventure in the spirit of the classic *Colossal Cave Adventure*, written in Java 25.
The project exists to showcase how AI coding assistants can be used to design, build, test and
evolve a program, so the code favours clarity over cleverness.

Deep below the mountain a dragon has stolen the gnomes' treasure, the *mountain gem*. Explore the
mines, pick up items, talk to the inhabitants, fight monsters, and carry the gem back out to the
mine entrance. Be careful: fights and falls can kill you.

## Requirements

- JDK 25
- Maven 3.9 or later

## Building and running

```powershell
mvn verify
```

```powershell
java -jar target/gnomish-mines-1.0.0-SNAPSHOT.jar
```

Pass `--seed <number>` to make all dice rolls repeatable, which is handy for demos:

```powershell
java -jar target/gnomish-mines-1.0.0-SNAPSHOT.jar --seed 42
```

You can also run it straight from Maven with `mvn -q compile exec:java`.

## How to play

| Command                                   | What it does                         |
| ----------------------------------------- | ------------------------------------ |
| `north`, `south`, `east`, `west`, `up`, `down` (or `n`, `s`, `e`, `w`, `u`, `d`) | Travel |
| `look`                                    | Describe your surroundings           |
| `examine <thing>`                         | Look closely at something            |
| `take <item>` / `take all`                | Pick things up (inventory is unlimited) |
| `drop <item>`                             | Put something down                   |
| `inventory` (`i`)                         | List what you carry                  |
| `talk <someone>`                          | Talk to an inhabitant                |
| `attack <monster>`                        | Fight a monster                      |
| `use` / `eat` / `drink <item>`            | Use an item, e.g. to heal            |
| `health`                                  | Show your health                     |
| `help`, `quit`                            | Show help, give up                   |

### Rules

- You start with 20 health. Health never goes above the maximum.
- Combat is turn based. You deal 1-4 damage plus the bonus of your best weapon, then the monster
  hits back if it is still alive.
- A living monster blocks some exits and won't let you pick anything up in its room.
- Some exits are dangerous. Slippery slopes always hurt, and some drops are deadly unless you
  carry the right equipment.
- Inhabitants say something new each time you talk to them, and some give you a gift.
- You win by reaching the mine entrance while carrying the mountain gem.

## Project layout

```
src/main/java/se/yrgo/gnomish/
  Main.java              Terminal entry point (the only class doing I/O)
  engine/                Game loop, command parser, dice
  model/                 Rooms, exits, items, monsters, inhabitants, player
  world/GnomishMines.java  The game map, built in plain Java
src/test/java/...        JUnit 6 + AssertJ tests
```

## Code quality tooling

`mvn verify` runs everything and fails on any problem:

- **Compiler** with `-Xlint:all`, treating warnings as errors.
- **JUnit 6** and **AssertJ** for tests.
- **Spotless** checks formatting with google-java-format. Fix formatting with `mvn spotless:apply`.
- **SpotBugs** static analysis at maximum effort and low threshold. Browse findings with
  `mvn spotbugs:gui`. Justified exclusions live in `spotbugs-exclude.xml`.
- **Maven Enforcer** checks the Java and Maven versions.

## Working with AI assistants

[`AGENTS.md`](AGENTS.md) holds the instructions AI coding agents should follow in this repository:
build commands, architecture, conventions and the definition of done.
