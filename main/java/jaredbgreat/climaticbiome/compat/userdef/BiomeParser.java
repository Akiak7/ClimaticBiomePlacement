package jaredbgreat.climaticbiome.compat.userdef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import jaredbgreat.climaticbiome.ClimaticBiomes;
import jaredbgreat.climaticbiome.configuration.ConfigHandler;
import jaredbgreat.climaticbiome.exception.BiomeReadingException;
import jaredbgreat.climaticbiome.generation.biome.BiomeList;
import jaredbgreat.climaticbiome.generation.biome.CentralDoubleBiome;
import jaredbgreat.climaticbiome.generation.biome.IBiomeSpecifier;
import jaredbgreat.climaticbiome.generation.biome.IslandBiome;
import jaredbgreat.climaticbiome.generation.biome.LeafBiome;
import jaredbgreat.climaticbiome.generation.biome.MesaDoubleBiome;
import jaredbgreat.climaticbiome.generation.biome.NoiseDoubleBiome;
import jaredbgreat.climaticbiome.generation.biome.SeedDoubleBiome;
import jaredbgreat.climaticbiome.generation.biome.TempDoubleBiome;
import jaredbgreat.climaticbiome.generation.biome.TerrainBiome;
import jaredbgreat.climaticbiome.generation.biome.WetDoubleBiome;
import jaredbgreat.climaticbiome.generation.biome.WeightedBiomeSpecifier;
import jaredbgreat.climaticbiome.generation.biome.biomes.GetTaiga.TaigaDoubleBiome;
import jaredbgreat.climaticbiome.util.Logging;
import jaredbgreat.dldungeons.util.parser.Tokenizer;
import net.minecraftforge.registries.IForgeRegistry;

public class BiomeParser {
	private IForgeRegistry biomeReg;
	private String fileDir;
	private interface ICommand {
		IBiomeSpecifier parse(String in);
	}
	private HashMap<String, ICommand> commands;
	private BufferedReader reader1;
	private BufferedReader reader2;
	
	
	public BiomeParser(IForgeRegistry reg, File dir, String sub) {
		biomeReg = reg;
		fileDir = dir.toString() + File.separator + "BiomeConfig" 
								 + File.separator + sub + File.separator;
		File fd = new File(fileDir);
		if(!fd.exists()) {
			fd.mkdirs();
		}
		commands = new HashMap<>();
		commands.put("biome", new LeafParse());
		commands.put("noise", new NoiseParse());
		commands.put("central", new CentralParse());
		commands.put("seed", new SeedParse());
		commands.put("temp", new TempParse());
		commands.put("taiga", new TaigaParse());
		commands.put("wetness", new WetParse());
		commands.put("island", new IslandParse());
		commands.put("plateau", new MesaParse());
		commands.put("terrain", new TerrainParse());
	}
	
	
	public void makeBiomeList(BiomeList list, String filename) {
		File file = new File(fileDir + filename);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		String line = null;
		Tokenizer tokens;		
		try {
			reader1 = new BufferedReader(new FileReader(file));
			while(reader1.ready()) {
				line = reader1.readLine().trim();
				if(line.isEmpty() || line.startsWith("#")) continue;
				tokens = new Tokenizer(line, "()");
				String tag = tokens.nextToken().toLowerCase().trim();
				try {
					// First create the specifier, then add it only if its valid
					IBiomeSpecifier biomeSpec = commands.get(tag).parse(tokens.nextToken());
					if(!biomeSpec.isEmpty()) {
						list.addItem(biomeSpec);
					} else {
		            	ClimaticBiomes.logger.error("\nFailed to load biome: \n"
		            			+ " \t Tag: " + tag + "\n"
		            			+ " \t Full String: " + line + "\n"
		            			+ " \t File: " + filename + "\n");	
		            	if(ConfigHandler.failfast) {
		            		throw new BiomeReadingException();
		            	}
					}
	            } catch (Exception e) {
	            	ClimaticBiomes.logger.error("\nFailed to load biome: \n"
	            			+ " \t Tag: " + tag + "\n"
	            			+ " \t Full String: " + line + "\n"
	            			+ " \t File: " + filename + "\n");						
	            	e.printStackTrace();
	                throw e;
	            }
			}
			reader1.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			reportError(e, file, line);
		}
	}
	
	
	public void addSpecialBiomes(BiomeList list, String filename) {
		File file = new File(fileDir + filename);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		String line = null;
		Tokenizer tokens;		
		try {
			reader2 = new BufferedReader(new FileReader(file));
			while(reader2.ready()) {
				line = reader2.readLine().trim();
				if(line.isEmpty() || line.startsWith("#")) continue;
				tokens = new Tokenizer(line, "()");
				String tag = tokens.nextToken().toLowerCase().trim();
				try {
					// First create the specifier, then add it only if its valid
					IBiomeSpecifier biomeSpec = commands.get(tag).parse(tokens.nextToken());
					if(!biomeSpec.isEmpty()) {
						list.addItem(biomeSpec);
					// These are quite typically not there, don't be strict
					} else if(ConfigHandler.failfast) {
		            	ClimaticBiomes.logger.error("\nFailed to load biome: \n"
		            			+ " \t Tag: " + tag + "\n"
		            			+ " \t Full String: " + line + "\n"
		            			+ " \t File: " + filename + "\n");
	            		throw new BiomeReadingException();
					}
	            } catch (Exception e) {
	            	ClimaticBiomes.logger.error("\nFailed to load biome: \n"
	            			+ " \t Tag: " + tag + "\n"
	            			+ " \t Full String: " + line + "\n"
	            			+ " \t File: " + filename + "\n");						
	            	e.printStackTrace();
	                throw e;
	            }
			}
			reader2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			reportError(e, file, line);
		}
	}
	
	
	private void reportError(RuntimeException ex, File file, String line) {
		StringBuilder b = new StringBuilder();
		b.append(System.lineSeparator());
		b.append("*****************************" + System.lineSeparator());
		b.append("   Error in file: " + file + System.lineSeparator());
		b.append("   " + line + System.lineSeparator());
		b.append("   Caused excpetion: " + ex + System.lineSeparator());		
		b.append("*****************************" + System.lineSeparator());
		b.append(System.lineSeparator());
		Logging.logError(b.toString());
		throw ex;
	}
	
	
        private final class NoiseParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String first = tokens.nextToken();
                        int boundary = Integer.parseInt(tokens.nextToken());
                        String second = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new NoiseDoubleBiome(first, boundary, second, biomeReg), weight);
                }
        }
	
	
        private final class CentralParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String first = tokens.nextToken();
                        int boundary = Integer.parseInt(tokens.nextToken());
                        String second = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new CentralDoubleBiome(first, boundary, second, biomeReg), weight);
                }
        }
	
	
        private final class MesaParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String first = tokens.nextToken();
                        int boundary = Integer.parseInt(tokens.nextToken());
                        String second = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new MesaDoubleBiome(first, boundary, second, biomeReg), weight);
                }
        }
	
	
	
        private final class SeedParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String first = tokens.nextToken();
                        int chance = Integer.parseInt(tokens.nextToken());
                        String second = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new SeedDoubleBiome(first, chance, second, biomeReg), weight);
                }
        }

	
        private final class TempParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String first = tokens.nextToken();
                        int boundary = Integer.parseInt(tokens.nextToken());
                        String second = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new TempDoubleBiome(first, boundary, second, biomeReg), weight);
                }
        }
	
	
        private final class TaigaParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String first = tokens.nextToken();
                        String second = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new TaigaDoubleBiome(first, second, biomeReg), weight);
                }
        }
	
	
        private final class WetParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String first = tokens.nextToken();
                        int boundary = Integer.parseInt(tokens.nextToken());
                        String second = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new WetDoubleBiome(first, boundary, second, biomeReg), weight);
                }
        }
	
	
        private final class LeafParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String biomeName = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new LeafBiome(biomeName, biomeReg), weight);
                }
        }
	
	
        private final class TerrainParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String biomeName = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new TerrainBiome(biomeName, biomeReg), weight);
                }
        }
	
	
        private final class IslandParse implements ICommand {
                public IBiomeSpecifier parse(String in) {
                        Tokenizer tokens = new Tokenizer(in, ", \t");
                        String biomeName = tokens.nextToken();
                        double weight = parseWeight(tokens, in);
                        return applyWeight(new IslandBiome(biomeName, biomeReg), weight);
                }
        }

        private double parseWeight(Tokenizer tokens, String original) {
                if((tokens != null) && tokens.hasMoreTokens()) {
                        String value = tokens.nextToken();
                        if(value != null && !value.isEmpty()) {
                                try {
                                        double weight = Double.parseDouble(value);
                                        if(weight < 0.0D) {
                                                ClimaticBiomes.logger.warn("Biome weight below zero in '{}'; treating as 0.0", original);
                                                return 0.0D;
                                        }
                                        return weight;
                                } catch (NumberFormatException e) {
                                        ClimaticBiomes.logger.warn("Failed to parse biome weight '{}' in '{}'; defaulting to 1.0", value, original);
                                        return 1.0D;
                                }
                        }
                }
                return 1.0D;
        }

        private IBiomeSpecifier applyWeight(IBiomeSpecifier spec, double weight) {
                double sanitized = (Double.isNaN(weight) || Double.isInfinite(weight)) ? 0.0D : weight;
                if(sanitized < 0.0D) {
                        sanitized = 0.0D;
                }
                if(sanitized == 1.0D) {
                        return spec;
                }
                return new WeightedBiomeSpecifier(spec, sanitized);
        }

}
