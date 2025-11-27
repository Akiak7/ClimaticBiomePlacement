package jaredbgreat.climaticbiome.generation.biome;

import jaredbgreat.climaticbiome.generation.mapgenerator.ChunkTile;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Predicate;

public class BiomeList implements IBiomeSpecifier {
        private final List<IBiomeSpecifier> list;
        private final List<Double> weights;
        private double totalWeight;
        private int entryCount;


        public BiomeList() {
                list = new ArrayList<>();
                weights = new ArrayList<>();
                totalWeight = 0.0D;
                entryCount = 0;
        }


        @Override
        public long getBiome(ChunkTile tile) {
                tile.nextBiomeSeed();
                try {
                        if(list.isEmpty()) {
                                throw new ArithmeticException("A biome was requested from an empty biome list!");
                        }
                        if(totalWeight <= 0.0D) {
                                return list.get(tile.getBiomeSeed() % list.size())
                                                .getBiome(tile);
                        }
                        double random = (tile.getBiomeSeed() & 0x7fffffffL);
                        double selection = (random * totalWeight) / (double)Integer.MAX_VALUE;
                        double cumulative = 0.0D;
                        for(int i = 0; i < list.size(); i++) {
                                cumulative += weights.get(i);
                                if(selection < cumulative) {
                                        return list.get(i).getBiome(tile);
                                }
                        }
                        return list.get(list.size() - 1).getBiome(tile);
                } catch (ArithmeticException ex) {
                        Logger.getLogger("Minecraft").log(Level.SEVERE,
                                        "A biome was requested from an empty biome list!  "
                                        + "\nAll lists must contain at least one biome (fix your configs)."
                                                        , ex);
                        throw ex;
                }
        }


        public void addItem(IBiomeSpecifier biome, int n) {
                addInternal(biome, biome.getWeight() * n);
        }


        public void addItem(IBiomeSpecifier biome) {
                addInternal(biome, biome.getWeight());
        }


        public void addItem(IBiomeSpecifier biome, double weight) {
                addInternal(biome, weight);
        }


        public void addItems(IBiomeSpecifier... biomes) {
                for(IBiomeSpecifier biome : biomes) {
                        addInternal(biome, biome.getWeight());
                }
        }


        public void addItems(List<IBiomeSpecifier> biomes) {
                for(IBiomeSpecifier biome : biomes) {
                        addInternal(biome, biome.getWeight());
                }
        }


        public boolean remove(IBiomeSpecifier bs) {
                IBiomeSpecifier target = (bs instanceof WeightedBiomeSpecifier)
                                ? ((WeightedBiomeSpecifier)bs).getDelegate()
                                : bs;
                int index = list.indexOf(target);
                if(index >= 0) {
                        double weight = weights.remove(index);
                        list.remove(index);
                        totalWeight -= weight;
                        entryCount = Math.max(0, entryCount - toSlotCount(weight));
                        return true;
                }
                return false;
        }


        public boolean merge(BiomeList other) {
                boolean changed = false;
                for(int i = 0; i < other.list.size(); i++) {
                        if(addInternal(other.list.get(i), other.weights.get(i))) {
                                changed = true;
                        }
                }
                return changed;
        }


    public boolean isEmpty() {
            return list.isEmpty();
    }


    public BiomeList filteredCopy(Predicate<IBiomeSpecifier> predicate) {
            BiomeList filtered = new BiomeList();
            for(int i = 0; i < list.size(); i++) {
                    IBiomeSpecifier entry = list.get(i);
                    if(predicate.test(entry)) {
                            filtered.addItem(entry, weights.get(i));
                    }
            }
            return filtered;
    }


        public int size() {
                return entryCount;
        }


        private boolean addInternal(IBiomeSpecifier biome, double weight) {
                double sanitized = sanitizeWeight(weight);
                if(sanitized <= 0.0D) {
                        return false;
                }
                IBiomeSpecifier actual = (biome instanceof WeightedBiomeSpecifier)
                                ? ((WeightedBiomeSpecifier)biome).getDelegate()
                                : biome;
                list.add(actual);
                weights.add(sanitized);
                totalWeight += sanitized;
                entryCount += toSlotCount(sanitized);
                return true;
        }


        private double sanitizeWeight(double weight) {
                if(Double.isNaN(weight) || Double.isInfinite(weight)) {
                        return 0.0D;
                }
                return (weight < 0.0D) ? 0.0D : weight;
        }


        private int toSlotCount(double weight) {
                if(weight <= 0.0D) {
                        return 0;
                }
                if(weight < 1.0D) {
                        return 1;
                }
                return (int)Math.round(weight);
        }

}
