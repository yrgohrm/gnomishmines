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

The jar is self-contained: the build shades its only dependency, [picocli](https://picocli.info/),
into it, so no classpath is needed.

Pass `--seed <number>` to make all dice rolls repeatable, which is handy for demos:

```powershell
java -jar target/gnomish-mines-1.0.0-SNAPSHOT.jar --seed 42
```

Use `--help` to list all options and `--version` to show the game version. You can also run it
straight from Maven with `mvn -q compile exec:java`.

### Colors

In an interactive terminal the game highlights important words with ANSI colors:

| What                                    | Color        |
| --------------------------------------- | ------------ |
| Room titles                             | bold         |
| People you can talk to                  | cyan         |
| Items                                   | yellow       |
| The mountain gem                        | bold magenta |
| Monsters                                | bold red     |
| Damage you deal, healing                | green        |
| Damage you take                         | red          |
| Health (good / low / critical)          | green / yellow / bold red |
| What inhabitants say                    | italic       |
| Winning / dying                         | bold green / bold red |

Colors are turned off automatically when the output is not an interactive terminal (for example
when input or output is piped or redirected), when the `NO_COLOR` environment variable is set to
a non-empty value, or when you pass `--no-color`:

```powershell
java -jar target/gnomish-mines-1.0.0-SNAPSHOT.jar --seed 42 --no-color
```

The legacy Windows console host may show raw escape codes such as `←[36m` instead of colors. Use
Windows Terminal, or pass `--no-color`.

## How to play

| Command                                                   | What it does                                  |
| --------------------------------------------------------- | --------------------------------------------- |
| `north`, `south`, `east`, `west`, `up`, `down` (or `n`, `s`, `e`, `w`, `u`, `d`) | Travel |
| `look`                                                    | Describe your surroundings                   |
| `examine <thing>`                                         | Look closely at something                    |
| `take <item>` / `take all`                                | Pick things up (inventory is unlimited)      |
| `drop <item>`                                              | Put something down                           |
| `inventory` (`i`)                                          | List what you carry                          |
| `talk <someone>`                                           | Talk to an inhabitant                        |
| `give <item> to <someone>`                                 | Give an inhabitant their requested item      |
| `date <someone>`                                           | Go on a date after giving their requested item |
| `romance <someone>`                                        | Start a romance after dating                 |
| `kill <target>`                                            | End your run, dismiss a sweetheart, or fight a monster |
| `attack <monster>`                                         | Fight a monster                              |
| `use` / `eat` / `drink <item>`                             | Use an item, e.g. to heal                    |
| `health`                                                   | Show your health                             |
| `help`, `quit`                                             | Show help, give up                           |

### Rules

- You start with 20 health. Health never goes above the maximum.
- Combat is turn based. You deal 1-4 damage plus the bonus of your best weapon, then the monster
  hits back if it is still alive.
- A living monster blocks some exits and won't let you pick anything up in its room.
- Some exits are dangerous. Slippery slopes always hurt, and some drops are deadly unless you
  carry the right equipment.
- Inhabitants say something new each time you talk to them, and some give you a gift.
- The four gnomes welcome wholesome, non-exclusive romances. Talk to a gnome, give them the item
  they request, then date them before starting a romance. The item is consumed and only unlocks
  that gnome. Give, date, and romance commands require the gnome to be in the room.
- The troll gun starts at the entrance. Firing it uses up the gun, sends any gnomes in the room
  offstage, and summons a troll you must fight. `kill <name>` also dismisses a romanceable gnome
  in your room; any gifted item still in the world can be recovered without making a duplicate.
- You win by reaching the mine entrance while carrying the mountain gem.

## Project layout

```
src/main/java/se/yrgo/gnomish/
  Main.java              Terminal entry point (the only class doing I/O)
  engine/                Game loop, command parser, dice
  model/                 Rooms, exits, items, monsters, inhabitants, player
  text/                  Text styles, e.g. ANSI colors for the terminal
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
