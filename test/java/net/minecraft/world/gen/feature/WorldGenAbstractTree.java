package net.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenAbstractTree {
    public abstract boolean generate(World worldIn, Random rand, BlockPos position);
}
