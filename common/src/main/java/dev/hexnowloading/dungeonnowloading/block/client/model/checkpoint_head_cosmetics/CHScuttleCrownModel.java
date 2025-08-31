package dev.hexnowloading.dungeonnowloading.block.client.model.checkpoint_head_cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class CHScuttleCrownModel extends Model implements ICheckpointCosmetic {
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

    @Override
    public void renderOverlay(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {

    }

    @Override
    public void emitAura(Level level, double cx, double cy, double cz,
                         Direction wallFacingOrNull, RandomSource rand, long gameTime) {
        // Use the same params you used before
        float  chance = 0.05f;   // 25% for lava+smoke
        double height = 0.75;    // vertical offset
        double spread = 0.20;    // horizontal spread

        // One call matches your original animateParticles behavior
        animateParticles(level, rand, cx, cy, cz, chance, height, spread);
    }

    /** Exact port of your previous helper (same coordinates & spread). */
    private static void animateParticles(Level level, RandomSource random,
                                         double cx, double cy, double cz,
                                         float chance, double height, double spread) {

        var data = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.LARGE_FLAME_PARTICLE.get(), 0.7F);

        // main scalable flame — always visible
        level.addAlwaysVisibleParticle(
                data,
                cx + (random.nextFloat() - 0.5D) * spread,
                cy + height + (random.nextFloat() - 0.5D) * 0.3D,
                cz + (random.nextFloat() - 0.5D) * spread,
                0.0, 0.0, 0.0
        );

        if (random.nextFloat() < chance) {
            double ox = (random.nextFloat() - 0.5D) * 0.3D;
            double oy = height + (random.nextFloat() - 0.5D) * 0.3D;
            double oz = (random.nextFloat() - 0.5D) * 0.3D;

            level.addParticle(ParticleTypes.LAVA, cx + ox, cy + oy, cz + oz, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx + ox, cy + oy, cz + oz, 0.0, 0.02, 0.0);
        }
    }
}
