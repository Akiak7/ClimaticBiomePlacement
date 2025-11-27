package jaredbgreat.climaticbiome.generation.cache;

public class AbstractWeaklyCacheable implements IHaveCoords {
        private final Coords coords;
        @SuppressWarnings("unused")
        private final WeakCache cache;
	
	
	public AbstractWeaklyCacheable(int x, int z, WeakCache holder) {
		coords = new Coords(x, z);
		cache  = holder;
	}


        @Override
        public Coords getCoords() {
                return coords;
        }

}
