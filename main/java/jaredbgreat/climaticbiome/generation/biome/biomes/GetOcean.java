package jaredbgreat.climaticbiome.generation.biome.biomes;

import jaredbgreat.climaticbiome.biomes.ModBiomes;
import jaredbgreat.climaticbiome.compat.userdef.DefReader;
import jaredbgreat.climaticbiome.configuration.ClimaticWorldSettings;
import jaredbgreat.climaticbiome.configuration.ConfigHandler;
import jaredbgreat.climaticbiome.generation.biome.BiomeClimateTable;
import jaredbgreat.climaticbiome.generation.biome.BiomeList;
import jaredbgreat.climaticbiome.generation.biome.IBiomeSpecifier;
import jaredbgreat.climaticbiome.generation.biome.LeafBiome;
import jaredbgreat.climaticbiome.generation.mapgenerator.ChunkTile;


public class GetOcean implements IBiomeSpecifier {
	private ClimaticWorldSettings  settings;
	private static GetOcean oceans;
	private int nearEdge, farEdge;
	private GetOcean(ClimaticWorldSettings settings) {
		super();
		this.settings = settings;
		nearEdge = 5 + settings.regionSize.whole;
		farEdge  = (256 * settings.regionSize.whole) - 6 - settings.regionSize.whole;
		init();
	}
	BiomeList frozen;
	BiomeList cold;
	BiomeList cool;
	BiomeList warm;
	BiomeList hot;
	BiomeList dfrozen;
	BiomeList dcold;
	BiomeList dcool;
	BiomeList dwarm;
	BiomeList dhot;
	IBiomeSpecifier islands1; // Main land biomes
	IBiomeSpecifier islands2; // Special island-only biomes
	IBiomeSpecifier beaches;
	
	long coasts;
	long fcoasts;
	
	boolean makeCoasts;
	
	public void init() {
		coasts  = ModBiomes.coast.getIdForBiome(ModBiomes.coast);
		fcoasts = ModBiomes.frozenCoast.getIdForBiome(ModBiomes.frozenCoast);
		makeCoasts = settings.hasCoasts;
		// Shallows
		frozen = new BiomeList();
		cold   = new BiomeList();
		cool   = new BiomeList();
		warm   = new BiomeList();
		hot    = new BiomeList();
		//Deeps
		dfrozen = new BiomeList();
		dcold   = new BiomeList();
		dcool   = new BiomeList();
		dwarm   = new BiomeList();
		dhot    = new BiomeList();
		// Islands
		islands1 = BiomeClimateTable.getLandTable();
		islands2 = GetIslands.getIslands();
		// Beaches
		beaches = GetBeach.getBeach();
		// Add biomes
		DefReader.readBiomeData(frozen,  "OceanFrozen.cfg");
		DefReader.readBiomeData(cold,    "OceanCold.cfg");
		DefReader.readBiomeData(cool,    "OceanCool.cfg");
		DefReader.readBiomeData(warm,    "OceanWarm.cfg");
		DefReader.readBiomeData(hot,     "OceanHot.cfg");
		DefReader.readBiomeData(dfrozen, "DeepOceanFrozen.cfg");
		DefReader.readBiomeData(dcold,   "DeepOceanCold.cfg");
		DefReader.readBiomeData(dcool,   "DeepOceanCool.cfg");
		DefReader.readBiomeData(dwarm,   "DeepOceanWarm.cfg");
		DefReader.readBiomeData(dhot,    "DeepOceanHot.cfg");
		if(warm.isEmpty()) {
			warm.addItem(new LeafBiome(0));
		}
		if(hot.isEmpty()) {
			hot.addItem(new LeafBiome(0));
		}
		if(dwarm.isEmpty()) {
			dwarm.addItem(new LeafBiome(24));
		}
		if(dhot.isEmpty()) {
			dhot.addItem(new LeafBiome(24));
		}
		if(cool.isEmpty()) {
			cool.addItem(new LeafBiome(0));
		}
		if(dcool.isEmpty()) {
			dcool.addItem(new LeafBiome(24));
		}
		if(frozen.isEmpty()) {
			frozen.addItem(new LeafBiome(10));
		}
		if(dfrozen.isEmpty()) {
			dfrozen.addItem(new LeafBiome(10));
		}
		// MUST BE LAST, ALWAYS!!!
		fixOceans();
	}	

	
        @Override
        public long getBiome(ChunkTile tile) {
                tile.setVanilla();
                int temp = tile.getTemp();
                int seed = tile.getBiomeSeed();
                int iceNoise = tile.getNoise();
                if(makeCoasts && tile.isBeach() && !tile.isRiver() && !swampy(tile)) {
                        tile.setSteep();
                if(((iceNoise / 2) - temp) > -1) {
                        return fcoasts;
                }
                        return coasts;
                }
                tile.nextBiomeSeed();
                double islandNoise = islandProbability(tile, seed);
                if(settings.addIslands && (islandNoise > 0.45) && notNearEdge(tile)) {
                        double selection = islandNoise + selectionJitter(seed);
                        if(selection > 0.85) {
                                if((tile.getTemp() > 9) && (tile.getTemp() < 19)
                                                                && (tile.getWet() > 3)) {
                                        if(islandNoise < 0.55) {
                                                return 14;
                                        }
                                        if(islandNoise < 0.7) {
                                                return 15;
                                        }
                                }
                                return 0;
                        } else if(settings.addIslands && (selection > 0.65)) {
                                if(islandNoise > shorelineBlend(tile)) {
                                        return islands1.getBiome(tile.nextBiomeSeed());
                                }
                        } else if(settings.addIslands && (selection > 0.5)) {
                                if(islandNoise > 0.5) {
                                        return islands2.getBiome(tile.nextBiomeSeed());
                                }
                        }
                        return getForIsland(tile);
                } else if((tile.getHeight()) < 0.2) {
                        return getDeepOcean(tile, temp, iceNoise);
                }
        return getShallowOcean(tile, temp, iceNoise);
    }

        private double islandProbability(ChunkTile tile, int seed) {
                double baseNoise = ((double)tile.getNoise()) / 9.0;
                double shoreline = shorelineBlend(tile);
                double heightBias = Math.min(1.0, Math.max(0.0, (tile.getHeight() + 0.2) * 1.8));
                double raw = (baseNoise * 0.7) + (heightBias * 0.3);
                double jitter = ((double)(seed & 0xffff)) / 0xffff;
                return Math.max(0.0, Math.min(1.0, (raw * 0.85) + (jitter * 0.15 * shoreline)));
        }

        private double shorelineBlend(ChunkTile tile) {
                double edge = Math.max(0.0, Math.min(1.0, (tile.getHeight() - 0.05) * 4.0));
                return Math.max(0.35, Math.min(0.95, edge));
        }

        private double selectionJitter(int seed) {
                return ((double)((seed >> 8) & 0xffff)) / 0xffff * 0.2;
        }
	
	
	public long getDeepOcean(ChunkTile tile, int temp, int iceNoise) {
        if(((iceNoise / 2) - temp) > -1) {
        	return dfrozen.getBiome(tile);
        }
    	if(temp < 7) {
    		return dcold.getBiome(tile);
    	} 
    	if(temp < 13) {
    		return dcool.getBiome(tile);        		
    	} 
    	if(temp < 19) {
    		return dwarm.getBiome(tile);
    	}
    	return dhot.getBiome(tile);
	}
	
	
	public long getShallowOcean(ChunkTile tile, int temp, int iceNoise) {
        if(((iceNoise / 2) - temp) > -1) {
        	return frozen.getBiome(tile);
        }
    	if(temp < 7) {
    		return cold.getBiome(tile);
    	} 
    	if(temp < 13) {
    		return cool.getBiome(tile);        		
    	} 
    	if(temp < 19) {
    		return warm.getBiome(tile);
    	}
    	return hot.getBiome(tile);
	}
	
	
	public static GetOcean getOcean(ClimaticWorldSettings settings) {
		if(oceans == null) {
			oceans = new GetOcean(settings);			
		}
		return oceans;
	}
	
	
	public static GetOcean getOceanForIslands() {
		return oceans;
	}
	
	
	/**
	 * This fixes possible problems with ocean,
	 * specifically, makes sure there are no 
	 * oceans types empty.  This way mods can 
	 * add temperature specific oceans, while 
	 * not relying on them.
	 */
	private void fixOceans() {
		if(warm.isEmpty()) {
			warm = cool;
		}
		if(hot.isEmpty()) {
			hot = warm;
		}
		if(cold.isEmpty()) {
			cold = cool;
		}
		if(frozen.isEmpty()) {
			// Should never be true, but just in case.
			frozen = cold; 
		}
		if(dcool.isEmpty()) {
			// Should never be true, but just in case.
			dcool = cool;
		}
		if(dwarm.isEmpty()) {
			dwarm = dcool;
		}
		if(dhot.isEmpty()) {
			dhot = dwarm;
		}
		if(dcold.isEmpty()) {
			dcold = dcool;
		}
		if(dfrozen.isEmpty()) {
			// Should never be true, but just in case.
			dfrozen = dcold; 
		}
	}
	
	/**
	 * This will get the oceans surrounding islands.
	 * 
	 * @param tile
	 * @return
	 */
	public long getForIsland(ChunkTile tile) {
		//tile.nextBiomeSeed();
		return getShallowOcean(tile, tile.getTemp(), tile.getNoise());
		//return coasts;
	}
	
	
	private boolean notNearEdge(ChunkTile tile) {
		int x = tile.getX();
		int z = tile.getZ();
		return ((x > nearEdge) && (x < farEdge)) && ((z > nearEdge) && (z < farEdge));
	}


	@Override
	public boolean isEmpty() {
		return false;
	}
	
	
	private boolean swampy(ChunkTile tile) {
		return ((tile.getTemp() > 7) 
				&& ((tile.getWet() - tile.getVal() - tile.getHeight()) > 0));
	}


}
