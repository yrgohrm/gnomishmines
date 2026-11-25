# Gnomish Mines Walkthrough

> [!WARNING]
> **SPOILER WARNING: THIS DOCUMENT GIVES AWAY THE WHOLE GAME.**
>
> It covers every room, every item, every character, every monster and the complete winning
> route, from the first step to the ending. If you want to explore the mines yourself, **stop
> reading now** and play first:
>
> ```powershell
> java -jar target/gnomish-mines-1.0.0-SNAPSHOT.jar
> ```
>
> Come back only if you get stuck.

<br>

---

<br>

## The goal

A dragon has stolen the gnomes' treasure, the **mountain gem**. Take it from the dragon and carry
it back out to the **Mine Entrance**. You win as soon as you step onto the entrance with the gem.

You start with 20 health, and health never goes above 20. You lose if it reaches 0.

## Map

```
UPPER LEVEL                                                  Dragon's Lair
                                                                   |
                    Chasm Edge ------- Troll Bridge ------- Crystal Cavern
                        |               (troll)
    Storeroom           |                               Smithy
        |  (spider)     |                                 |
  Mushroom Grotto --- Main Tunnel ------------------ Gnome Village
                        |
                  Mine Entrance

LOWER LEVEL

  Hermit's Hollow ----------------------------------- Underground Lake

UP AND DOWN
  Chasm Edge    <-->  Hermit's Hollow    needs the rope both ways, deadly without it going down
  Gnome Village <-->  Underground Lake   safe stairs
  Main Tunnel    -->  Underground Lake   slippery slope, one way, 3 damage
  Troll Bridge   -->  Hermit's Hollow    a deadly jump, never do this
```

In words:

| Room             | Exits                                                                     |
| ---------------- | ------------------------------------------------------------------------- |
| Mine Entrance    | north: Main Tunnel                                                        |
| Main Tunnel      | south: Entrance, east: Gnome Village, west: Mushroom Grotto, north: Chasm Edge, down: Underground Lake (slippery slope, 3 damage) |
| Gnome Village    | west: Main Tunnel, north: Smithy, down: Underground Lake                  |
| Smithy           | south: Gnome Village                                                      |
| Mushroom Grotto  | east: Main Tunnel, north: Storeroom (blocked by the spider)               |
| Storeroom        | south: Mushroom Grotto                                                    |
| Chasm Edge       | south: Main Tunnel, east: Troll Bridge, down: Hermit's Hollow (deadly without the rope) |
| Hermit's Hollow  | up: Chasm Edge (needs the rope), east: Underground Lake                   |
| Underground Lake | west: Hermit's Hollow, up: Gnome Village                                  |
| Troll Bridge     | west: Chasm Edge, east: Crystal Cavern (blocked by the troll), down: Hermit's Hollow (deadly) |
| Crystal Cavern   | west: Troll Bridge, north: Dragon's Lair                                  |
| Dragon's Lair    | south: Crystal Cavern                                                     |

## Everything in the mines

### Weapons

You always fight with your best weapon automatically. A hit deals 1-4 damage plus the weapon's
bonus.

| Weapon     | Bonus | Where                                  |
| ---------- | ----- | -------------------------------------- |
| bare fists | +0    | -                                      |
| pickaxe    | +2    | Mine Entrance, on the ground           |
| iron sword | +4    | Gift from Tink in the Smithy           |
| rune axe   | +6    | Gift from Morg in Hermit's Hollow      |

### Healing items

Use them with `eat`, `drink` or `use`. Using an item never gives a monster a free hit.

| Item        | Heals | Where                                         |
| ----------- | ----- | --------------------------------------------- |
| bread       | 3     | Mine Entrance                                 |
| mushroom    | 4     | Mushroom Grotto (after the spider is dead)    |
| smoked fish | 6     | Gift from Pip at the Underground Lake         |
| potion      | 10    | Storeroom                                     |
| potion      | 10    | Hermit's Hollow                               |

### Other items

| Item          | Where                                  | Use                                     |
| ------------- | -------------------------------------- | --------------------------------------- |
| rope          | Storeroom                              | Lets you climb down to and up from Hermit's Hollow |
| gold coins    | Dropped by the troll                   | Just a souvenir                         |
| crystal shard | Crystal Cavern                         | Just a souvenir                         |
| mountain gem  | Dragon's Lair                          | The treasure. Bring it to the entrance  |

### Characters

Each character says something new every time you talk to them. Some give a gift the first time.

| Character | Where            | Gift you get | Gift they want | What they tell you                                   |
| --------- | ---------------- | ------------ | -------------- | ---------------------------------------------------- |
| Bramble   | Gnome Village    | -            | crystal shard  | The story, and hints about Tink, the axe and the rope |
| Tink      | Smithy           | iron sword   | gold coins     | Some encouragement                                   |
| Morg      | Hermit's Hollow  | rune axe     | smoked fish    | The troll is strong but stupid, the dragon is not    |
| Pip       | Underground Lake | smoked fish  | mushroom       | The hermit is friendly                               |

### Dating and romance

Talk to a gnome to learn what they want. Give them that item while you are together; the gift is
consumed and unlocks dating with that gnome only. You must date them before starting a romance.
Give, date, and romance commands work only when the gnome is in the room. Relationships are
independent, so you can romance all four. Keep scenes wholesome and enjoy their character-specific
moments.

### Monsters

A living monster blocks an exit and won't let you pick anything up in its room. It hits back
every time you attack and fail to kill it.

| Monster | Where           | Health | Hits for | Drops      |
| ------- | --------------- | ------ | -------- | ---------- |
| spider  | Mushroom Grotto | 6      | 1-3      | -          |
| troll   | Troll Bridge    | 14     | 2-4      | gold coins |
| dragon  | Dragon's Lair   | 24     | 2-6      | -          |

### Deadly traps

- **Climbing down from Chasm Edge without the rope** kills you.
- **Jumping down from Troll Bridge** kills you, rope or no rope.
- **The slippery slope** down from the Main Tunnel costs 3 health. It won't kill a healthy
  adventurer, but there is never a reason to take it.

## Step-by-step walkthrough

### 1. Gear up at the entrance

```
take all
```

You pick up the **pickaxe** and the **bread**.

### 2. Visit the Gnome Village and the Smithy

```
north
east
talk bramble
talk bramble
talk bramble
```

Talking to Bramble is optional, but his three lines explain the story and point you to everything
you need.

```
north
talk tink
south
```

Tink gives you the **iron sword**.

### 3. Defeat the spider and raid the Storeroom

```
west
west
attack spider
```

With the sword you deal 5-8 damage, so the 6-health spider falls in one or two hits. Repeat
`attack spider` until it is defeated, then:

```
take all
north
take all
south
```

You get the **mushroom**, the **rope** and a **potion**.

### 4. Climb down to the hermit

```
east
north
down
```

Because you carry the rope, you tie it to the iron ring and climb safely into Hermit's Hollow.

```
talk morg
take potion
```

Morg gives you the **rune axe**, the best weapon in the game. Grab the second **potion** too.

### 5. Optional: visit Pip for a free fish

From Hermit's Hollow, take the long way back:

```
east
talk pip
up
west
north
```

Pip gives you the **smoked fish**, and you walk back to Chasm Edge via the Gnome Village. If you
skip this, just climb back up the rope instead:

```
up
```

### 6. Defeat the troll

From Chasm Edge:

```
east
attack troll
attack troll
```

The rune axe deals 7-10 damage, so the 14-health troll needs two hits and gets at most one swing
back (up to 4 damage). Pick up its loot if you like, then cross the bridge:

```
take all
east
take all
```

You collect the **gold coins** and the **crystal shard**. Do **not** go down from the bridge.

### 7. Heal before the dragon

```
health
```

Top yourself up to 20 health before you go north. Use the small items first so the potions are
not wasted, for example `eat bread`, `eat mushroom` or `eat fish`.

### 8. Slay the dragon

```
north
attack dragon
```

Keep attacking until the dragon is defeated. It has 24 health, so it takes three or four hits
with the rune axe, and it can hit back for up to 6 each time.

**The one rule that keeps you alive:** if your health drops to 6 or below, drink a potion before
you attack again (`drink potion`). Healing does not give the dragon a free hit. Starting at full
health with two potions in your pack, you can't lose this fight.

```
take gem
```

You now hold the **mountain gem**.

### 9. Walk out into the daylight

```
south
west
west
south
south
```

Crystal Cavern, Troll Bridge, Chasm Edge, Main Tunnel, Mine Entrance. The moment you arrive at
the entrance with the gem, you win:

> You step out into the daylight carrying the mountain gem. The gnomes will sing of you for a
> hundred years. YOU WIN!

## Speedrun route

The shortest winning route skips the optional chats and the fish. Repeat each `attack` line until
the monster is defeated, and heal if you get low against the dragon.

```
take all
n
e
n
talk tink
s
w
w
attack spider
n
take all
s
e
n
d
talk morg
u
e
attack troll
e
n
attack dragon
take gem
s
w
w
s
s
```
