package net.minecraft.world.biome;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class Biome {
    public Object topBlock;
    public Object fillerBlock;
    public Object decorator;

    private static final Map<Biome, Integer> IDS = new IdentityHashMap<>();
    private static int NEXT_ID = 1;

    protected Biome(BiomeProperties properties) {}

    public static int getIdForBiome(Biome biome) {
        if(biome == null) {
            return 0;
        }
        Integer id = IDS.get(biome);
        if(id == null) {
            id = NEXT_ID++;
            IDS.put(biome, id);
        }
        return id;
    }

    public void genTerrainBlocks(World worldIn, java.util.Random rand,
            ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {}

    public static class BiomeProperties {
        private final String name;

        public BiomeProperties(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
