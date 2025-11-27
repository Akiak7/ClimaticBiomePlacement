This folder contains subfolders for every biome mod that you may have installed. The 'Minecraft' folder is for vanilla biomes. The 'custom' folder is for any additional biome mods. The 'special' folder is for biomes from non-biome mods.

Inside each folder, you will find config files for the different biome categories. Open each file, and add, edit, or remove entries as you please.

To use the correct names for the biomes, please check the "BiomeList.txt" file in the root folder. This will list all the installed biomes.

*****

Supported syntax:

- biome(id)

Simply inserts the biome.

- terrain(id)

Same as biome(id), but forces the tile's terrain type to "normal", ignoring any steep/plateau terrain hints for that section.

- island(id)

Can be used in ocean biome tables to create biome islands when noise surpasses an internal threshold. For island biomes it is usually preferred to just use biome(id).

- noise(firstId, threshold, secondId)

If the noise value of the tile (0-9) is below the threshold, the first biome is chosen, otherwise the second is chosen. Useful for combining variants of biomes.

- central(edgeId, threshold, centerId)

Same as noise(), but favours the first biome near the landmass center, and the second near the landmass edge. Threshold (0-19) determines balance between edge and center. Useful for edge variants of biomes.

- plateau(topBiome, threshold, bottomBiome)

Same as noise(), but forces steep, mesa-like terrain. Useful for canyons, mesas, or any other biomes that require steep terrain changes.

- seed(firstId, chance, secondId)

Places the first biome 1 out of <chance> times, otherwise places the second biome. Useful for specifying rare variants of a biome.

- temp(coldId, threshold, warmId)

Chooses the first biome if the local temperature is below the threshold (0-24), otherwise chooses the second biome. Useful for temperature variants. See below for temperature bands.

- taiga(snowId, mildId)

Same as temp(), but uses an internal temperature boundary to specify snowy variants of biomes (6 when BoP is enabled, 7 otherwise). This only exists so that the boundary automatically updates when BoP's extended climate table is or isn't being used.

- wetness(dryBiome, threshold, wetBiome)

Chooses the first biome if the local humidity is below the threshold (0-9) otherwise chooses the second biome.

*****

WEIGHTS: every single command above also takes an additional value between 0.0 and 1.0, that specifies the likelihood of that entry being chosen overall. For example, biome(id) can be extended to biome(id, weight), and noise() can be extended to noise(firstId, threshold, secondId, weight).

NESTING: commands can be nested. For example, you could write noise(seed(A, X, B), Z, temp(C, Y, B)).

TEMPERATURE BANDS

With BoP not enabled:

0-4: Arctic (called “tundra,” it includes all the ice biomes)
5-9: Subarctic: Taiga and cold plains
10-14: Temperate: Forest and plains
15-19: Subtropical: Warm Forest, plains / savannas, scrub, and deserts
20-24: Tropical: Jungles and savannas

With BoP enabled:

0-3: Arctic
4-7: Subarctic
8-11: Cool Temperate: Cool forest and cool plains
12-15: Temperate
16-20: Subtropical
21-24: Tropical

Oceans and special islands are different:

Frozen: Based on a noise function
Cold: temperature < 6
Cool: temperature < 12; vanilla ocean (pre-1.13) is here
Warm: temperature < 18
Hot: temperature at least 18

For swamps the temperature works like this:

Cold: temperature < 12
Cool: temperature < 16
Warm: temperature < 21
Hot: temperature at least 21

If ocean lists are empty the lists will spread out from OceanCool.  If a swamp list is empty it will spread out from SwampWarm.  If a special islands list is empty it will use normal islands instead; that is, it will use mainland biomes.

Wetness is on a scale of one to ten, but the actual meaning is relative to the climate zone it appears in.  Specifically, since most real-world deserts are in the subtropics, low wetness means a drier climate than the same number somewhere else.

There are a few special lists.  Alpine is for mountains and is divided into dry and wet, with treed mountains treated as wet.  ChaparralScrub is the list for scrub, bush, and chaparral biomes that appear between plains and deserts (mostly in the subtropics).  Parkland refers to an area between forest and plains biomes in the temperate zone; it may pick either a forest or plains biome, but is also where open forests go.  The special islands list are for biomes that should usually exist as islands; if a biome has “island” or “archepeligo” in its name it probably belongs here.
