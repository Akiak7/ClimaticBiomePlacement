package jaredbgreat.climaticbiome.generation.chunk;

import static jaredbgreat.climaticbiome.generation.chunk.ChunkGenClimaticRealistic.WATER;

import jaredbgreat.climaticbiome.generation.ClimaticBiomeProvider;
import jaredbgreat.climaticbiome.util.SpatialHash;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class ImprovedBlockSetter implements IBlockSetter {
	static final IBlockState STONE = Blocks.STONE.getDefaultState();
	private final HeightMapManager heightMapManager;
	private final VolumnMapManager volMapManager;
	private final SpatialHash sprandom;
	private final World world;	
	
	
	public ImprovedBlockSetter(World world, SpatialHash sprandom) {
		this.world = world;
		this.sprandom = sprandom;
		heightMapManager = new HeightMapManager();
		volMapManager = new VolumnMapManager();
	}
	
	
    public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
    	int[][] heightmap = getHeihtmapForChunk(x, z, sprandom);
        for(int i = 0; i < 16; i++) {
            for(int k = 0; k < 16; k++) {
                int index = (i * 16) + k;
                int height = heightmap[0][index];
                int cappedHeight = Math.min(height, 256);

                for (int y = 0; y < cappedHeight; y++) {
                    primer.setBlockState(i, y, k, STONE);
                }

                if(height < 63) {
                    int waterStart = Math.max(height, 0);
                    for (int y = waterStart; y < 63; y++) {
                        primer.setBlockState(i, y, k, WATER);
                    }
                }
            }
        }
    }
    
    
    private int[][] getHeihtmapForChunk(int x, int z, SpatialHash rand) {
    	ClimaticBiomeProvider provider = (ClimaticBiomeProvider)world.getBiomeProvider();
    	return heightMapManager.getChunkHieghts(x, z, rand, provider.getTerrainBiomeGen(x, z));
    }

}
