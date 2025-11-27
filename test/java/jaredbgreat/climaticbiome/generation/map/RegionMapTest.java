package jaredbgreat.climaticbiome.generation.map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegionMapTest {

    @Test
    public void setBiomeExpressStoresBaseAndVariantTogether() {
        RegionMap map = new RegionMap(0, 0, 1);
        long fullBiome = 0xAB34L;

        map.setBiomeExpress(fullBiome, 0);

        assertEquals(0x34, map.getBiome(0, 0));
        assertEquals(0xAB, map.getSubBiomeId(0, 0));
        assertEquals((int)fullBiome, map.getFullBiome(0, 0));
    }
}
