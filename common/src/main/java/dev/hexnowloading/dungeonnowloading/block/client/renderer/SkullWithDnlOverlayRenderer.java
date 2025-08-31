package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics.CHScuttleCrownModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics.ICheckpointCosmetic;
import dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics.aura_only.CHFlameAura;
import dev.hexnowloading.dungeonnowloading.block.entity.CheckpointHeadBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class SkullWithDnlOverlayRenderer implements BlockEntityRenderer<SkullBlockEntity> {
    private final SkullBlockRenderer vanilla;
    private final CHScuttleCrownModel crownModel;
    private final ICheckpointCosmetic flameAura = new CHFlameAura();
    private static final ResourceLocation CROWN_TEX =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/checkpoint_head/checkpoint_head_scuttle_crown.png");

    public SkullWithDnlOverlayRenderer(BlockEntityRendererProvider.Context ctx) {
        this.vanilla = new SkullBlockRenderer(ctx); // delegate base skull
        this.crownModel = new CHScuttleCrownModel(ctx.bakeLayer(CHScuttleCrownModel.LAYER_LOCATION));
    }

    @Override
    public void render(SkullBlockEntity be, float partial, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        // 1) vanilla base
        vanilla.render(be, partial, pose, buffers, light, overlay);

        var st = be.getBlockState();
        var block = st.getBlock();
        boolean isDnl = block == DNLBlocks.CHECKPOINT_HEAD.get() || block == DNLBlocks.CHECKPOINT_WALL_HEAD.get();
        if (!isDnl) return;

        // 2) cosmetic lookup (null-safe + trim)
        String cosmeticRaw = (be instanceof dev.hexnowloading.dungeonnowloading.block.entity.CheckpointHeadBlockEntity ch)
                ? ch.getCosmeticId() : null;
        String cosmetic = (be instanceof CheckpointHeadBlockEntity ch) ? ch.getActiveCosmeticId() : null;


        ICheckpointCosmetic cosmeticModel = null;
        boolean drawCrown = false;

        if ("scuttle_crown".equalsIgnoreCase(cosmetic)) {
            cosmeticModel = crownModel;
            drawCrown = true;                 // overlay mesh
        } else if ("flame".equalsIgnoreCase(cosmetic)) {
            cosmeticModel = flameAura;        // aura-only, no mesh
        }

        if (cosmeticModel == null) return;

        // 3) ONLY do overlay transforms + crown draw if needed
        if (drawCrown) {
            net.minecraft.core.Direction wall = (block instanceof WallSkullBlock)
                    ? st.getValue(WallSkullBlock.FACING) : null;

            int seg = (wall == null)
                    ? st.getValue(SkullBlock.ROTATION)
                    : net.minecraft.world.level.block.state.properties.RotationSegment.convertToSegment(wall.getOpposite());

            float yaw = net.minecraft.world.level.block.state.properties.RotationSegment.convertToDegrees(seg);

            pose.pushPose();
            if (wall == null) {
                pose.translate(0.5F, 0.0F, 0.5F);
            } else {
                pose.translate(0.5F - wall.getStepX() * 0.25F, 0.25F, 0.5F - wall.getStepZ() * 0.25F);
            }
            pose.scale(-1.0F, -1.0F, 1.0F);
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw));

            // lift crown above head
            pose.translate(0.0F, -1.5F, 0.0F);

            VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(CROWN_TEX));
            crownModel.renderToBuffer(pose, vc, light, overlay, 1, 1, 1, 1);
            pose.popPose();
        }

        // 4) powered aura (shared)
        var level = be.getLevel();
        if (level != null && level.isClientSide && level.hasNeighborSignal(be.getBlockPos())) {
            long t = level.getGameTime();
            if ((t & 3L) == 0L) { // throttle
                var pos = be.getBlockPos();
                double cx = pos.getX() + 0.5;
                double cy = pos.getY();       // keep your original baseline
                double cz = pos.getZ() + 0.5;
                cosmeticModel.emitAura(level, cx, cy, cz, null, level.getRandom(), t);
            }
        }
    }
}
