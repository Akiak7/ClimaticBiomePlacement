package jaredbgreat.climaticbiome.generation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import jaredbgreat.climaticbiome.generation.map.IMapRegistry;
import jaredbgreat.climaticbiome.generation.map.MapRegistry;
import jaredbgreat.climaticbiome.generation.map.NewMapRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;


public class ClimaticBiomeProvider extends BiomeProvider {
        private World world;
        private IMapRegistry finder;
        private boolean vanillaCacheValid;
        private boolean altChunks;
        private final Map<Long, Biome[]> chunkCache = new LinkedHashMap<Long, Biome[]>(16, 0.75f, true) {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, Biome[]> eldest) {
                        return size() > 64;
                }
        };
        
        
        public ClimaticBiomeProvider(World world, boolean altChunks) {
                super(/*world.getWorldInfo()*/);
                vanillaCacheValid = true;
                this.world = world;
                this.altChunks = altChunks;
                {
	                if(net.minecraftforge.fml.common.Loader.isModLoaded("jeid")) {
	                	finder = new NewMapRegistry(world.getSeed(), world, altChunks);
	                } else {
	                	finder = new MapRegistry(world.getSeed(), world, altChunks);
	                }
                }
        }
        

    @Nullable
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, 
                        List<Biome> biomes, Random random) {
        int i = x - range >> 2;
        int j = z - range >> 2;
        int k = x + range >> 2;
        int l = z + range >> 2;
        int i1 = k - i + 1;
        int j1 = l - j + 1;
        Biome[] barr = new Biome[i1 * j1];
        barr = this.getBiomes(barr, i, j, i1, j1);
        BlockPos blockpos = null;
        int k1 = 0;

        for (int l1 = 0; l1 < i1 * j1; ++l1) {
            int i2 = i + l1 % i1 << 2;
            int j2 = j + l1 / i1 << 2;
            Biome biome = barr[l1];

            if (biomes.contains(biome) && (blockpos == null || random.nextInt(k1 + 1) == 0)) {
                blockpos = new BlockPos(i2, 0, j2);
                ++k1;
            }
        }
        return blockpos;
    }


    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        if(biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }
        for(int dz = 0; dz < height; dz++) {
                for(int dx = 0; dx < width; dx++) {
                        int blockX = (x + dx) * 4;
                        int blockZ = (z + dz) * 4;
                        int chunkX = blockX >> 4;
                        int chunkZ = blockZ >> 4;
                        Biome[] chunk = getChunkBiomes(chunkX, chunkZ);
                        biomes[(dz * width) + dx] = chunk[(chunkModulus(blockZ) * 16) + chunkModulus(blockX)];
                }
        }
        return biomes;
    }


    public float[][] getdataForGeneration(float[][] biomes, int x, int z, int width, int height) {
        if(biomes == null || biomes.length < width * height) {
            biomes = new float[width * height][];
        }
        for(int i = 0; i < width; i++) 
                for(int j = 0; j < height; j++) {
                        //System.err.println(findBiomeAt((x + i) * 4, (z + j) * 4));
                        biomes[(j * width) + i] = finder.getHeightData((x + i) * 4, (z + j) * 4);
                }
        return biomes;
    }
    
    
    private Biome findBiomeAt(int x, int z) {
        Biome[] chunk = getChunkBiomes(x >> 4, z >> 4);
        return chunk[(chunkModulus(z) * 16) + chunkModulus(x)];
    }
    

    @Override
    public Biome[] getBiomes(@Nullable Biome[] in, int x, int z, int width, int depth, boolean cacheFlag) {
        //IntCache.resetIntCache();
        if((in == null) || (in.length < (width * depth))) {
            in = new Biome[width * depth];
        }
        if(isExactChunkRequest(x, z, width, depth)) {
                finder.getChunkBiomeGrid(x / 16, z / 16, in);
        } else {
                finder.getUnalignedBiomeGrid(x, z, width, depth, in);
        }
        return in;
    }

    private boolean isExactChunkRequest(int x, int z, int width, int depth) {
        return (width == 16) && (depth == 16) && ((x & 15) == 0) && ((z & 15) == 0);
    }
    
    
    private int getIDForCoords(int x, int z) {
        return Biome.getIdForBiome(getBiome(new BlockPos(x * 4, 64, z * 4)));
    }
    
    
    private int modRight4(int in) {
        return in & 3;
    }
    
    
    private int chunkModulus(int in) {
        return in & 0xf;
    }

    private Biome[] getChunkBiomes(int chunkX, int chunkZ) {
        long key = chunkKey(chunkX, chunkZ);
        Biome[] chunk = chunkCache.get(key);
        if(chunk == null) {
                chunk = new Biome[256];
                finder.getChunkBiomeGen(chunkX, chunkZ, chunk);
                chunkCache.put(key, chunk);
        }
        return chunk;
    }

    private long chunkKey(int chunkX, int chunkZ) {
        return ((long)chunkX << 32) | (chunkZ & 0xffffffffL);
    }

    
    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        x /= 16;
        z /= 16;
        int cr = radius >> 2;
        if(cr > 0) {
            int i1 = x - cr;
            int j1 = z - cr;
            int i2 = x + cr + 1;
            int j2 = z + cr + 1;
                for(int i = i1; i < i2; i++)
                        for(int j = j1; j < j2; j++) {
                                if(!allowed.contains(finder.getBiomeChunk(i, j))) {
	                                return false;
	                        }
	                }
        }
        return allowed.contains(finder.getBiomeChunk(x, z));
    }

    
    public boolean areBiomesViable(int x, int z, int radius, Type allowed) {
        x /= 16;
        z /= 16;
        int cr = radius >> 2;
        if(cr > 0) {
            int i1 = x - cr;
            int j1 = z - cr;
            int i2 = x + cr + 1;
            int j2 = z + cr + 1;
                for(int i = i1; i < i2; i++)
                        for(int j = j1; j < j2; j++) {
                                if(!BiomeDictionary.hasType(finder.getBiomeChunk(i, j), allowed)) {
	                                return false;
	                        }
	                }
        }
        return BiomeDictionary.hasType(finder.getBiomeChunk(x, z), allowed);
    }

    
    public boolean areChunkBiomesViable(int chunkX, int chunkZ, List<Biome> allowed) {
        return allowed.contains(finder.getBiomeChunk(chunkX, chunkZ));
    }

    
    public boolean areChunkBiomesViable(int chunkX, int chunkZ, Type allowed) {
        return BiomeDictionary.hasType(finder.getBiomeChunk(chunkX, chunkZ), allowed);
    }
    
    
    public float[] getTerrainBiomeGen(int x, int z) {
    	return finder.getTerrainBiomeGen(x, z, new float[512]);
    }

    
    public void cleanupCache() {
        if(vanillaCacheValid) try {
                super.cleanupCache();
        } catch (Exception e) {
                // This is hacky and only testing will tell
                // if I needed to call the super class method;
                // i.e., if not doing so will cause memory
                // problems due to Minecraft creating such data
                // on its own.
                vanillaCacheValid = false;
                System.err.println("Error cleaning up vanilla biome cache; "
                                + "should I be trying to do that?!"); 
        }
        finder.cleanCaches();
    }
    
    
    public boolean isAltChunks() {
    	return altChunks;
    }

    


}
