package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics.CHScuttleCrownModel;
import dev.hexnowloading.dungeonnowloading.block.entity.CheckpointHeadBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CheckpointHeadRenderer implements BlockEntityRenderer<CheckpointHeadBlockEntity> {
    private final SkullBlockRenderer vanillaSkull;
    private final CHScuttleCrownModel crownModel;
    private static final ResourceLocation CROWN_TEX =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/checkpoint_head/checkpoint_head_scuttle_crown.png");

    public CheckpointHeadRenderer(BlockEntityRendererProvider.Context ctx) {
        this.vanillaSkull = new SkullBlockRenderer(ctx); // delegate to vanilla for the base head
        this.crownModel   = new CHScuttleCrownModel(ctx.bakeLayer(CHScuttleCrownModel.LAYER_LOCATION));
    }

    @Override
    public void render(CheckpointHeadBlockEntity be, float partial, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        // base skull
        vanillaSkull.render(be, partial, pose, buffers, light, overlay);

        // cosmetic gate
        // if (!"scuttle_crown".equals(be.getCosmeticId())) return;

        BlockState st = be.getBlockState();
        Direction wallFacing = st.getBlock() instanceof WallSkullBlock
                ? st.getValue(WallSkullBlock.FACING) : null;

        // vanilla yaw (0..15 -> degrees)
        float yawDeg = 0f;
        if (wallFacing == null) {
            int seg = st.getValue(SkullBlock.ROTATION);
            yawDeg = net.minecraft.world.level.block.state.properties.RotationSegment.convertToDegrees(seg);
        } else {
            // wall skulls face outward already; we’ll still rotate to match model heading
            int seg = net.minecraft.world.level.block.state.properties.RotationSegment.convertToSegment(wallFacing.getOpposite());
            yawDeg = net.minecraft.world.level.block.state.properties.RotationSegment.convertToDegrees(seg);
        }

        pose.pushPose();
        if (wallFacing == null) {
            // standing
            pose.translate(0.5F, 0.0F, 0.5F);
        } else {
            // wall (push out from wall like vanilla)
            pose.translate(0.5F - wallFacing.getStepX() * 0.25F, 0.25F, 0.5F - wallFacing.getStepZ() * 0.25F);
        }

        // vanilla flip (head models are authored in flipped space)
        pose.scale(-1.0F, -1.0F, 1.0F);

        // rotate to face the same way as the skull model
        pose.mulPose(Axis.YP.rotationDegrees(yawDeg));

        // lift crown above the head top
        pose.translate(0.0F, -0.25F, 0.0F); // NOTE: after the -Y scale, up is negative

        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(CROWN_TEX));
        crownModel.renderToBuffer(pose, vc, light, overlay, 1f, 1f, 1f, 1f);
        pose.popPose();

        if (be.getLevel() != null && be.getLevel().isClientSide && (be.getLevel().getGameTime() % 20 == 0)) {
            DungeonNowLoading.LOGGER.debug("CheckpointHeadRenderer: pos={}, cosmetic={}", be.getBlockPos(), be.getCosmeticId());
        }

    }

}
