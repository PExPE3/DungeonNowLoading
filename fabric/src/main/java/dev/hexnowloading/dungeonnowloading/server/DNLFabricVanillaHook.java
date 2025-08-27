package dev.hexnowloading.dungeonnowloading.server;

import com.google.common.collect.ImmutableSet;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedHashSet;
import java.util.Set;

public class DNLFabricVanillaHook {
    public static void setup() {
        extendSkullValidBlocks();
    }

    private static void extendSkullValidBlocks() {
        // Extend vanilla SKULL valid blocks
        Set<Block> old = BlockEntityType.SKULL.validBlocks;
        Set<Block> plus = new LinkedHashSet<>(old);
        plus.add(DNLBlocks.CHECKPOINT_HEAD.get());
        plus.add(DNLBlocks.CHECKPOINT_WALL_HEAD.get());
        BlockEntityType.SKULL.validBlocks = ImmutableSet.copyOf(plus);
    }
}
