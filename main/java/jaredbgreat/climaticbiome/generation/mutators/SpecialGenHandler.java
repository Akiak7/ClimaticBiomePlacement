package jaredbgreat.climaticbiome.generation.mutators;

import java.util.Random;

import jaredbgreat.climaticbiome.generation.ClimaticBiomeProvider;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
// import net.minecraftforge.fml.common.registry.GameRegistry;

public class SpecialGenHandler implements IWorldGenerator  {


        public SpecialGenHandler() {
                // This registration is temporarily disabled until the stored seed data
                // required by reallyGenerate() is available. Leaving it enabled would cause
                // unnecessary per-chunk callbacks during world generation. Once the special
                // generation feature is ready, re-enable the line below.
                // GameRegistry.registerWorldGenerator(this, 100);
        }

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, 
				IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if(world.isRemote) return;
		BiomeProvider provider = world.getBiomeProvider();
		if(provider instanceof ClimaticBiomeProvider) {
			reallyGenerate(random, chunkX, chunkZ, world, chunkGenerator, 
					chunkProvider, (ClimaticBiomeProvider)provider);
		}		
	}
	
	
	private void reallyGenerate(Random random, int chunkX, int chunkZ, World world, 
				IChunkGenerator chunkGenerator, IChunkProvider chunkProvider, 
				ClimaticBiomeProvider provider) {
		//TODO: Make it work.  Get seed data, use it for this.  (It must be stored, first.)
	}

}
