package se.yrgo.gnomish.world;

import static se.yrgo.gnomish.model.Direction.DOWN;
import static se.yrgo.gnomish.model.Direction.EAST;
import static se.yrgo.gnomish.model.Direction.NORTH;
import static se.yrgo.gnomish.model.Direction.SOUTH;
import static se.yrgo.gnomish.model.Direction.UP;
import static se.yrgo.gnomish.model.Direction.WEST;

import java.util.List;
import se.yrgo.gnomish.model.Exit;
import se.yrgo.gnomish.model.Inhabitant;
import se.yrgo.gnomish.model.Item;
import se.yrgo.gnomish.model.Monster;
import se.yrgo.gnomish.model.Room;
import se.yrgo.gnomish.model.World;

/**
 * Builds the Gnomish Mines, the world of the game.
 *
 * <ul>
 *   <li>Mine Entrance (start and goal): north to Main Tunnel.
 *   <li>Main Tunnel: east to Gnome Village, west to Mushroom Grotto, north to Chasm Edge, down a
 *       slippery slope (3 damage) to Underground Lake.
 *   <li>Gnome Village: north to Smithy, down the stairs to Underground Lake.
 *   <li>Mushroom Grotto: north to Storeroom, guarded by a spider.
 *   <li>Chasm Edge: east to Troll Bridge, down to Hermit's Hollow (deadly without the rope).
 *   <li>Hermit's Hollow: up to Chasm Edge (needs the rope), east to Underground Lake.
 *   <li>Troll Bridge: east to Crystal Cavern, guarded by a troll; down is a deadly jump.
 *   <li>Crystal Cavern: north to Dragon's Lair, where the dragon guards the treasure.
 * </ul>
 */
public final class GnomishMines {

  /** The treasure the player must bring back to the entrance. */
  public static final String TREASURE = "mountain gem";

  /** The player's starting and maximum health. */
  public static final int MAX_HEALTH = 20;

  /** Damage from falls that nobody survives. */
  static final int DEADLY_FALL = 100;

  private GnomishMines() {}

  /** Creates a fresh copy of the world. Every call returns a new, independent world. */
  public static World create() {
    Room entrance =
        new Room(
            "Mine Entrance",
            "You stand at the mouth of an old gnomish mine. Cold air drifts up from the darkness"
                + " to the north. Behind you, sunlight and the open mountainside.");
    Room tunnel =
        new Room(
            "Main Tunnel",
            "A wide tunnel shored up with ancient timber. Lanterns flicker along the walls."
                + " A slick, wet slope leads down into the dark.");
    Room village =
        new Room(
            "Gnome Village",
            "Tiny stone houses with round doors line a cosy cavern. Mushroom lamps glow softly"
                + " and narrow stairs wind down into the rock.");
    Room smithy =
        new Room(
            "Smithy",
            "A hot, clanging workshop. An anvil sits beside a roaring forge, and half-finished"
                + " blades hang from the ceiling.");
    Room grotto =
        new Room(
            "Mushroom Grotto",
            "Giant mushrooms tower over you, shedding a pale blue light. Thick webs stretch"
                + " across a passage to the north.");
    Room storeroom =
        new Room(
            "Storeroom",
            "Dusty shelves hold forgotten mining supplies. Something has been nesting here.");
    Room chasmEdge =
        new Room(
            "Chasm Edge",
            "The tunnel ends at the lip of a bottomless-looking chasm. An iron ring is"
                + " hammered into the rock beside the drop. A rickety bridge leads east.");
    Room hollow =
        new Room(
            "Hermit's Hollow",
            "The bottom of the chasm. Bones of unlucky miners lie scattered among the rocks."
                + " A small camp has been set up against the wall.");
    Room lake =
        new Room(
            "Underground Lake",
            "Black water stretches out of sight. Stairs carved by gnomes lead up, and a"
                + " tunnel heads west.");
    Room bridge =
        new Room(
            "Troll Bridge",
            "A swaying bridge of rope and planks spans the chasm. Far below, you can just make"
                + " out a campfire.");
    Room cavern =
        new Room(
            "Crystal Cavern",
            "Crystals as tall as gnomes jut from every surface, splitting the light into"
                + " rainbows. You smell smoke from the north.");
    Room lair =
        new Room(
            "Dragon's Lair",
            "A vast, scorched cavern. Heaps of gnomish gold glitter in the gloom.");

    entrance
        .connect(NORTH, Exit.open(tunnel))
        .addItem(Item.weapon("pickaxe", "A rusty but sturdy pickaxe. Better than fists.", 2))
        .addItem(Item.consumable("bread", "A crusty loaf. Restores 3 health.", 3));

    tunnel
        .connect(SOUTH, Exit.open(entrance))
        .connect(EAST, Exit.open(village))
        .connect(WEST, Exit.open(grotto))
        .connect(NORTH, Exit.open(chasmEdge))
        .connect(
            DOWN,
            Exit.hazardous(
                lake, 3, "You slip on the wet stones and tumble all the way down the slope!"));

    village
        .connect(WEST, Exit.open(tunnel))
        .connect(NORTH, Exit.open(smithy))
        .connect(DOWN, Exit.open(lake))
        .addInhabitant(
            new Inhabitant(
                "Bramble",
                "An ancient gnome with a beard down to his boots.",
                List.of(
                    "A dragon stole the Heart of the Mountain, our mountain gem! Bring it back"
                        + " to the surface and the curse on these mines will lift.",
                    "You'll never beat the dragon with your bare hands. Visit Tink in the"
                        + " smithy, and they say the hermit at the bottom of the chasm keeps"
                        + " an old dwarven axe.",
                    "Only a fool jumps into the chasm. Find a rope! I think there was one in"
                        + " the old storeroom past the grotto.")));

    smithy
        .connect(SOUTH, Exit.open(village))
        .addInhabitant(
            new Inhabitant(
                "Tink",
                "A soot-covered gnome swinging a hammer twice her size.",
                List.of(
                    "Off to fight the dragon? Ha! Take this, you'll need it more than me.",
                    "Keep the blade sharp and your head down."),
                Item.weapon("iron sword", "A short but well balanced gnomish blade.", 4)));

    grotto
        .connect(EAST, Exit.open(tunnel))
        .connect(NORTH, Exit.open(storeroom))
        .addItem(Item.consumable("mushroom", "A glowing blue mushroom. Restores 4 health.", 4))
        .setMonster(
            new Monster("spider", "A giant cave spider clicks its fangs at you.", 6, 1, 3), NORTH);

    storeroom
        .connect(SOUTH, Exit.open(grotto))
        .addItem(Item.plain("rope", "A long coil of sturdy rope."))
        .addItem(Item.consumable("potion", "A red healing potion. Restores 10 health.", 10));

    chasmEdge
        .connect(SOUTH, Exit.open(tunnel))
        .connect(EAST, Exit.open(bridge))
        .connect(
            DOWN,
            Exit.dangerousWithout(
                hollow,
                "rope",
                DEADLY_FALL,
                "You tie the rope to the iron ring and climb carefully down into the chasm.",
                "You try to climb down without a rope. Your fingers slip..."));

    hollow
        .connect(
            UP,
            Exit.requires(
                chasmEdge,
                "rope",
                "You climb back up the rope.",
                "The walls are far too steep to climb without a rope."))
        .connect(EAST, Exit.open(lake))
        .addInhabitant(
            new Inhabitant(
                "Morg",
                "A wild-haired hermit warming his hands over a small fire.",
                List.of(
                    "Visitors! Nobody visits Morg. Here, take my old axe. It has slain a"
                        + " dragon before, it can do it again.",
                    "The troll on the bridge is strong but stupid. The dragon is neither."),
                Item.weapon("rune axe", "A heavy dwarven axe covered in glowing runes.", 6)))
        .addItem(Item.consumable("potion", "A red healing potion. Restores 10 health.", 10));

    lake.connect(WEST, Exit.open(hollow))
        .connect(UP, Exit.open(village))
        .addInhabitant(
            new Inhabitant(
                "Pip",
                "A young gnome dangling a fishing line into the water.",
                List.of(
                    "Shh, you'll scare the fish! Oh, you look hungry. Have one of mine.",
                    "The hermit to the west is friendly, honest. Just a bit smelly."),
                Item.consumable("smoked fish", "A delicious smoked fish. Restores 6 health.", 6)));

    bridge
        .connect(WEST, Exit.open(chasmEdge))
        .connect(EAST, Exit.open(cavern))
        .connect(
            DOWN,
            Exit.hazardous(hollow, DEADLY_FALL, "You leap from the bridge into the darkness!"))
        .setMonster(
            new Monster(
                "troll",
                "A huge, warty troll leans on a club.",
                14,
                2,
                4,
                Item.plain("gold coins", "A pouch of gold coins the troll collected as tolls.")),
            EAST);

    cavern
        .connect(WEST, Exit.open(bridge))
        .connect(NORTH, Exit.open(lair))
        .addItem(Item.plain("crystal shard", "A shard of crystal that hums faintly."));

    lair.connect(SOUTH, Exit.open(cavern))
        .addItem(
            Item.plain(TREASURE, "The Heart of the Mountain, a fist-sized gem that glows warmly."))
        .setMonster(
            new Monster(
                "dragon", "A red dragon lies coiled on its hoard, watching you.", 24, 2, 6));

    return new World(entrance, entrance, TREASURE, MAX_HEALTH);
  }
}
