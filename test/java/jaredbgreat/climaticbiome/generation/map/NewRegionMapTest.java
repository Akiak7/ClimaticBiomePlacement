package jaredbgreat.climaticbiome.generation.map;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NewRegionMapTest {

    @Test
    public void equalsMatchesSameTypeAndCoords() {
        NewRegionMap first = new NewRegionMap(3, 4, 2);
        NewRegionMap second = new NewRegionMap(3, 4, 2);

        assertTrue(first.equals(second));
    }

    @Test
    public void equalsReturnsFalseForDifferentCoords() {
        NewRegionMap first = new NewRegionMap(1, 2, 2);
        NewRegionMap second = new NewRegionMap(2, 1, 2);

        assertFalse(first.equals(second));
    }

    @Test
    public void equalsHandlesOtherRegionImplementation() {
        NewRegionMap map = new NewRegionMap(5, 6, 2);
        RegionMap other = new RegionMap(5, 6, 2);

        assertTrue(map.equals(other));
    }

    @Test
    public void equalsReturnsFalseForNonRegionMap() {
        NewRegionMap map = new NewRegionMap(7, 8, 2);

        assertFalse(map.equals("not a map"));
    }
}
