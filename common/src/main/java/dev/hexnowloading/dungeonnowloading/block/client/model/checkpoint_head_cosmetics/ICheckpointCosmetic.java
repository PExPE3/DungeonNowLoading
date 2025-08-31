package dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public interface ICheckpointCosmetic {
    /** Render the overlay (already positioned/rotated to skull space). */
    void renderOverlay(PoseStack pose, MultiBufferSource buffers, int light, int overlay);

    /** Emit cosmetic-specific aura particles around (cx,cy,cz). */
    void emitAura(Level level, double cx, double cy, double cz,
                  Direction wallFacingOrNull, RandomSource rand, long gameTime);
}
