package jaredbgreat.climaticbiome.biomes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.biome.Biome;

public final class SubBiomeRegistryTest {
    private SubBiomeRegistryTest() {}

    public static void main(String[] args) throws Exception {
        resetSingleton();
        SubBiomeRegistry registry = SubBiomeRegistry.getSubBiomeRegistry();
        int initialLength = getBackingArray(registry).length;

        TestBiome parent = new TestBiome();
        List<SubBiome> registered = new ArrayList<>();
        for(int i = 0; i < 400; i++) {
            SubBiome sub = new SubBiome(parent, i, new Biome.BiomeProperties("test-" + i));
            registered.add(sub);
            registry.add(sub);
        }

        SubBiome[] data = getBackingArray(registry);
        if(data.length <= initialLength) {
            throw new AssertionError("Expected backing array to grow beyond " + initialLength
                    + ", but found length " + data.length);
        }

        for(SubBiome sub : registered) {
            SubBiome stored = registry.get(sub.getSubId());
            if(stored != sub) {
                throw new AssertionError("Registry failed to return stored instance for id " + sub.getSubId());
            }
        }

        System.out.println("SubBiomeRegistry growth test passed with backing array length " + data.length);
    }

    private static void resetSingleton() throws Exception {
        Field instance = SubBiomeRegistry.class.getDeclaredField("subreg");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    private static SubBiome[] getBackingArray(SubBiomeRegistry registry) throws Exception {
        Field dataField = SubBiomeRegistry.class.getDeclaredField("data");
        dataField.setAccessible(true);
        return (SubBiome[])dataField.get(registry);
    }

    private static class TestBiome extends Biome {
        TestBiome() {
            super(new Biome.BiomeProperties("test"));
        }
    }
}
