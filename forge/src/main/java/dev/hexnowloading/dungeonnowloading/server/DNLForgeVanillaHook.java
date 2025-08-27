package dev.hexnowloading.dungeonnowloading.server;

import com.google.common.collect.ImmutableSet;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class DNLForgeVanillaHook {
    private DNLForgeVanillaHook() {}

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            // AT must be applied so validBlocks is accessible & mutable.
            var old  = BlockEntityType.SKULL.validBlocks;
            var plus = new java.util.LinkedHashSet<>(old);
            plus.add(DNLBlocks.CHECKPOINT_HEAD.get());
            plus.add(DNLBlocks.CHECKPOINT_WALL_HEAD.get());
            BlockEntityType.SKULL.validBlocks = ImmutableSet.copyOf(plus);
        });
    }
}
