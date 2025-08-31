package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.entity.CheckpointHeadBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin<T extends BlockEntity> {

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void dnl$create(BlockPos pos, BlockState state, CallbackInfoReturnable<T> cir) {
        BlockEntityType<?> self = (BlockEntityType<?>)(Object)this;
        if (self == BlockEntityType.SKULL) {
            var b = state.getBlock();
            if (b == DNLBlocks.CHECKPOINT_HEAD.get() || b == DNLBlocks.CHECKPOINT_WALL_HEAD.get()) {
                cir.setReturnValue((T)new CheckpointHeadBlockEntity(pos, state));
            }
        }
    }
}