package net.minecraft.world.biome;

public class BiomeForest extends Biome {
    public enum Type {
        NORMAL
    }

    public BiomeForest(BiomeProperties properties, Type type) {
        super(properties);
    }
}
