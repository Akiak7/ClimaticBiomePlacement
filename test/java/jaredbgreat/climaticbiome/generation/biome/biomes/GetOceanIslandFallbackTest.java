package jaredbgreat.climaticbiome.generation.biome.biomes;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;

import jaredbgreat.climaticbiome.generation.biome.BiomeList;
import jaredbgreat.climaticbiome.generation.biome.IslandBiome;
import jaredbgreat.climaticbiome.generation.biome.LeafBiome;
import jaredbgreat.climaticbiome.generation.mapgenerator.ChunkTile;
import sun.misc.Unsafe;

public class GetOceanIslandFallbackTest {

    @Test
    public void islandFallbackAvoidsRecursiveIslandSelection() throws Exception {
        Unsafe unsafe = getUnsafe();
        GetOcean ocean = (GetOcean)unsafe.allocateInstance(GetOcean.class);

        setField(ocean, "frozenNoIslands", singleLeafList(1));
        setField(ocean, "coldNoIslands", singleLeafList(2));
        setField(ocean, "coolNoIslands", singleLeafList(9));
        setField(ocean, "warmNoIslands", singleLeafList(4));
        setField(ocean, "hotNoIslands", singleLeafList(5));

        BiomeList coolWithIsland = new BiomeList();
        coolWithIsland.addItem(new IslandBiome(77));
        setField(ocean, "cool", coolWithIsland);

        Field oceansField = GetOcean.class.getDeclaredField("oceans");
        oceansField.setAccessible(true);
        Object originalOcean = oceansField.get(null);
        oceansField.set(null, ocean);

        try {
            ChunkTile tile = new ChunkTile(0, 0, 0, 0);
            setIntField(tile, "temp", 10);
            setIntField(tile, "noiseVal", 0);

            IslandBiome islandSpecifier = new IslandBiome(123);

            long biome = islandSpecifier.getBiome(tile);

            assertEquals(9L, biome);
        } finally {
            oceansField.set(null, originalOcean);
        }
    }

    private static BiomeList singleLeafList(long biomeId) {
        BiomeList list = new BiomeList();
        list.addItem(new LeafBiome(biomeId));
        return list;
    }

    private static Unsafe getUnsafe() throws Exception {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe)theUnsafe.get(null);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void setIntField(Object target, String fieldName, int value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(target, value);
    }
}
