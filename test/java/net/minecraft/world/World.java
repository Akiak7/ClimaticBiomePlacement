package net.minecraft.world;

import net.minecraft.world.storage.WorldInfo;

public class World {
    private final long seed = 0L;
    private final WorldInfo worldInfo = new WorldInfo();

    public long getSeed() {
        return seed;
    }

    public WorldInfo getWorldInfo() {
        return worldInfo;
    }
}
