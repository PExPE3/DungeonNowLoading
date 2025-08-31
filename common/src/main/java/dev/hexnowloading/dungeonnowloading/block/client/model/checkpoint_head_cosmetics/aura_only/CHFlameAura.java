package dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics.aura_only;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics.ICheckpointCosmetic;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class CHFlameAura implements ICheckpointCosmetic {
    @Override
    public void renderOverlay(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        // No model to render (intentionally empty)
    }

    @Override
    public void emitAura(Level level, double cx, double cy, double cz,
                         Direction wallFacingOrNull, RandomSource rand, long gameTime) {
        // Same baseline as your previous helper: block center, Y = block pos + height
        final double height = 0.1;  // vertical offset above skull base
        final double radius = 0.5;  // ring radius
        final int    count  = rand.nextInt(2); // 7–9 flames per tick

        // Slow rotation so the ring feels alive
        final double angBase = gameTime * 0.12;

        for (int i = 0; i < count; i++) {
            double ang = angBase + i * (Math.PI * 2.0 / count) + rand.nextDouble() * 0.25;
            double ox  = Math.cos(ang) * radius + (rand.nextDouble() - 0.5) * 0.03;
            double oz  = Math.sin(ang) * radius + (rand.nextDouble() - 0.5) * 0.03;
            double oy  = height + (rand.nextDouble() - 0.5) * 0.06;

            // Flame with a gentle upward drift
            double vy = 0.004 + rand.nextDouble() * 0.006;
            level.addParticle(ParticleTypes.FLAME, cx + ox, cy + oy, cz + oz, 0.0, vy, 0.0);
        }
    }
}
