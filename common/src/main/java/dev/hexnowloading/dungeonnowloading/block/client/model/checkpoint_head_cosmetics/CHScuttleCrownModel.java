package dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class CHScuttleCrownModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "checkpoint_head/checkpoint_head_scuttle_crown"), "main");
    private final ModelPart crown;

    public CHScuttleCrownModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.crown = root.getChild("crown");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition crown = partdefinition.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(-3.0F, -1.5F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay,
                               float r, float g, float b, float a) {
        pose.pushPose();

        // scale about the head-top pivot so the crown stays seated
        float s = 1.25f;           // 25% bigger
        float px = 1.0f / 16.0f;   // pixels -> blocks
        float yPivotPx = 15.0f;    // head top in skull/head space (~ -8 px)
        // move pivot to origin -> scale -> move back
        pose.translate(0.0f, yPivotPx * px, 0.0f);
        pose.scale(s, s, s);
        pose.translate(0.0f, -yPivotPx * px, 0.0f);

        // optional micro-adjust if you want it tighter/looser on the head:
        // pose.translate(0.0f, -0.01f, 0.0f); // after vanilla -Y flip, up is negative

        crown.render(pose, vc, light, overlay, r, g, b, a);
        pose.popPose();
    }
}
