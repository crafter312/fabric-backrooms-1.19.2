# fabric-backrooms-1.19.2
My take on a Backrooms mod for minecraft fabric!

CURRENT FEATURES
----------------

Ores, materials, and machines extending TechReborn's features with the end product of creating Async's KV31 low-proximity magnetic distortion system (aka the Backrooms door).

These include but are not limited to:
  - Bismuthinite ore
  - Bismuth ingot
  - Indium ingot
  - Industrial alloy smelter
  - Low-temperature solder alloy ingot
  - Superconducting magnet coil
  - Superconducting magnet
  - LN2 coolant cell
  - Toroidal field model coil (TFMC) magnet
  - KV31 control computer
  - KV31 portal multiblock structure

Backrooms level 0 dimension:
  - yellow wallpaper block
  - gray ceiling tile block
  - sprawling complex of rooms made from randomly placed and sized wall blocks made from yellow wallpaper
  - "level_0" biome with lights
  - "level_0_dark" biome with no lights
  - block state for ceiling tiles where lights are placed
  - when placing light feature, test for light block state so that light features aren't placed on top of each other
  - dedicated fluorescent light block
  - small chance that when a light block is placed, it flickers (done with a block state, luminance function to return light level, different darker textures for each light level when flickering)
  - added rare light feature with larger chance of flickering light, found in both normal and dark biomes
  - added thin straight wall feature
  - added thin crooked wall feature
  - added threshold chamber structure
    - designed according to Kane Pixel's backrooms series
    - contains pre-built destination portal
    - any KV31 portal built in the overworld links to one of these threshold chamber portals
    - portals built in range of the same threshold chamber all link to the same destination portal
    - new portals built inside Level 0 will not work
    - however, any non-activated threshold chamber portals in Level 0 can be re-activated by placing a KV31 control computer next to it

Other features:
  - "no-clipping" (teleporting) into Backrooms Level 0 when taking suffocation or void damage; this currently happens 1% of the time (might change in the future)


IN PROGRESS FEATURES
--------------------

The following features are currently in progress or next on the list of things to implement:
  - Backrooms Level 0 dimension
    - custom colored carpet block
    - other less common features, like grids of wall pillars in large open spaces or something like that (to make things more interesting)
    - experiment with biome noise settings to make dark biomes smaller (or both biomes smaller, whichever one I manage to do first)


PLANNED FEATURES
----------------

The following are some features I've thought of which I hope to implement in the near future:
  - Backrooms Level 0 entity (as shown in Kane Pixels' YouTube series)
  - If possible, apply some kind of color map to the carpets in level 0 so they appear stained?
  - More backrooms dimensions, according to the standard Backrooms wiki (https://backrooms.fandom.com/wiki/Backrooms_Wiki)
    - specifically, minimum dimensions required to get back to the overworld via dimension 3999
    - one possible route is the following:
      - 0, 1, 2, 4, 11, 3999
      - this route depends on implementing windows and weather in level 4
    - another possible route is the following:
      - 0, 1, 2, 4, 6, 8, 9, 11, 3999
      - this route depends partially on implementing dimension-wide lights out feature in level 4
    - additionally, one could also visit level 5 in between 4 and 6 by entering a specific door, but level 5 sounds complicated to generate and is not necessary in the near future


FAQ
---

Q: How many levels do you currently have?
A: So far I only have level 0, but the details aren't entirely finished yet.

Q: How can I get to level 0?
A: You can get there by constructing the KV31 multiblock structure, by taking suffocation or void damage, or by using the following command: /execute in backrooms:level_0 run tp ~ 20 ~. The 20 y value is important because that is the y value Level 0 is located at.
