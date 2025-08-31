package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics.CHScuttleCrownModel;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class SkullWithDnlOverlayRenderer implements BlockEntityRenderer<SkullBlockEntity> {
    private final SkullBlockRenderer vanilla;
    private final CHScuttleCrownModel crownModel;
    private static final ResourceLocation CROWN_TEX =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/checkpoint_head/checkpoint_head_scuttle_crown.png");

    public SkullWithDnlOverlayRenderer(BlockEntityRendererProvider.Context ctx) {
        this.vanilla = new SkullBlockRenderer(ctx); // delegate base skull
        this.crownModel = new CHScuttleCrownModel(ctx.bakeLayer(CHScuttleCrownModel.LAYER_LOCATION));
    }

    @Override
    public void render(SkullBlockEntity be, float partial, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        // Always render base vanilla skull first
        vanilla.render(be, partial, pose, buffers, light, overlay);

        // Only overlay on OUR blocks
        var st = be.getBlockState();
        var b = st.getBlock();
        boolean isDnlHead =
                b == DNLBlocks.CHECKPOINT_HEAD.get() || b == DNLBlocks.CHECKPOINT_WALL_HEAD.get();
        if (!isDnlHead) return;

        // If you need the cosmetic id, the placed BE is actually your subclass.
        String cosmetic = null;
        if (be instanceof dev.hexnowloading.dungeonnowloading.block.entity.CheckpointHeadBlockEntity ch) {
            cosmetic = ch.getCosmeticId();
        }
        if (!"scuttle_crown".equals(cosmetic)) return;

        // Replicate vanilla transforms
        Direction wall = (b instanceof WallSkullBlock) ? st.getValue(WallSkullBlock.FACING) : null;
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
        pose.mulPose(Axis.YP.rotationDegrees(yaw));

        // sit above skull top (after -Y flip, "up" is negative)
        pose.translate(0.0F, -1.5F, 0.0F);

        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(CROWN_TEX));
        crownModel.renderToBuffer(pose, vc, light, overlay, 1, 1, 1, 1);
        pose.popPose();
    }
}
