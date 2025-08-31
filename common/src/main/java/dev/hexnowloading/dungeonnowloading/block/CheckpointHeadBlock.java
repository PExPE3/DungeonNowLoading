package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.CheckpointHeadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CheckpointHeadBlock extends PlayerHeadBlock implements EntityBlock {


    public CheckpointHeadBlock(Properties props) { super(props); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CheckpointHeadBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CheckpointHeadBlockEntity head) {
                // Cache name + lore
                head.setDisplayFromItem(stack);
                // NEW: read cosmetics list / active index from the item if present
                head.setCosmeticsFromItem(stack);
                head.syncToClient();
            }
        }
    }

    /** Middle-click pick-block keeps Name, Lore, and Cosmetics */
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CheckpointHeadBlockEntity head) {
            head.writeDisplayToItem(stack);     // preserves Name + Lore
            head.writeCosmeticsToItem(stack);   // NEW: preserves cosmetics list + index
        }
        return stack;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        // Brush = cycle the active cosmetic
        if (held.getItem() instanceof BrushItem) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof CheckpointHeadBlockEntity head) {
                    boolean changed = head.cycleCosmetic();
                    if (changed) {
                        String current = String.valueOf(head.getActiveCosmeticId());
                        // actionbar feedback
                        player.displayClientMessage(Component.literal("Cosmetic: " + current), true);
                        level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.6f, 1.0f);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Normal right-click: print the lore line to the player’s chat
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CheckpointHeadBlockEntity head) {
            Component line = head.buildRightClickLine();
            if (line != null) player.sendSystemMessage(line);
        }
        return InteractionResult.CONSUME;
    }
}