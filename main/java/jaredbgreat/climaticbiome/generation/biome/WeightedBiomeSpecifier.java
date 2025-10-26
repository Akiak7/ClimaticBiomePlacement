package jaredbgreat.climaticbiome.generation.biome;

import jaredbgreat.climaticbiome.generation.mapgenerator.ChunkTile;

/**
 * Wraps another {@link IBiomeSpecifier} while attaching a weighting value used
 * when selecting entries from {@link BiomeList}.  The delegate handles all
 * biome selection logic; this wrapper simply exposes the configured weight so
 * that the {@link BiomeList} can honour user supplied probabilities.
 */
public class WeightedBiomeSpecifier implements IBiomeSpecifier {
        private final IBiomeSpecifier delegate;
        private final double weight;

        public WeightedBiomeSpecifier(IBiomeSpecifier delegate, double weight) {
                this.delegate = delegate;
                double sanitized = (Double.isNaN(weight) || Double.isInfinite(weight))
                                ? 0.0D : weight;
                this.weight = (sanitized < 0.0D) ? 0.0D : sanitized;
        }

        @Override
        public long getBiome(ChunkTile tile) {
                return delegate.getBiome(tile);
        }

        @Override
        public boolean isEmpty() {
                return delegate.isEmpty();
        }

        @Override
        public double getWeight() {
                return weight;
        }

        public IBiomeSpecifier getDelegate() {
                return delegate;
        }
}
