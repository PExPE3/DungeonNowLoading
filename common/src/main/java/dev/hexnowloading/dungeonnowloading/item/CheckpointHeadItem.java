package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.block.entity.CheckpointHeadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CheckpointHeadItem extends StandingAndWallBlockItem {

    public CheckpointHeadItem(Block standing, Block wall, Properties props) {
        super(standing, wall, props, Direction.DOWN); // instrument arg ignored visually for skull items
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
        boolean changed = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide) {
            var be = level.getBlockEntity(pos);
            if (be instanceof CheckpointHeadBlockEntity head) {
                head.setLoreFromItem(stack);
                changed = true;
            }
        }
        return changed;
    }


}
