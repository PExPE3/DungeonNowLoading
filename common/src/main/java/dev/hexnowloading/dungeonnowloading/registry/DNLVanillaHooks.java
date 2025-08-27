package dev.hexnowloading.dungeonnowloading.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedHashSet;
import java.util.Set;

public class DNLVanillaHooks {

    private DNLVanillaHooks() {}

    public static void extendSkullValidBlocks() {
        // Extend vanilla SKULL valid blocks
        Set<Block> old = BlockEntityType.SKULL.vali
        Set<Block> plus = new LinkedHashSet<>(old);
        plus.add(DNLBlocks.CHECKPOINT_HEAD.get());
        plus.add(DNLBlocks.CHECKPOINT_WALL_HEAD.get());
        BlockEntityType.SKULL.validBlocks = ImmutableSet.copyOf(plus);*/
    }
}
