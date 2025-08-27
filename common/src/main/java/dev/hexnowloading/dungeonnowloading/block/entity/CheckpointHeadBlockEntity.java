package dev.hexnowloading.dungeonnowloading.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CheckpointHeadBlockEntity extends SkullBlockEntity {
    private ListTag savedLore; // list of STRING tags (JSON text components)

    public CheckpointHeadBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public CheckpointHeadBlockEntity(BlockPos pos, BlockState state, GameProfile owner) {
        super(pos, state);
        setOwner(owner);
    }

    /** Read lore off an item on placement */
    public void setLoreFromItem(ItemStack stack) {
        CompoundTag display = stack.getTagElement("display");
        if (display != null && display.contains("Lore", Tag.TAG_LIST)) {
            savedLore = display.getList("Lore", Tag.TAG_STRING).copy();
        } else {
            savedLore = null;
        }
        setChanged();
    }

    /** Write cached lore back to an item for drops / pick-block */
    public void writeLoreToItem(ItemStack stack) {
        if (savedLore != null && !savedLore.isEmpty()) {
            stack.getOrCreateTagElement("display").put("Lore", savedLore.copy());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (savedLore != null && !savedLore.isEmpty()) {
            // Keep the same nesting ("display.Lore") so loot copy_nbt can work too
            CompoundTag display = tag.getCompound("display");
            display.put("Lore", savedLore.copy());
            tag.put("display", display);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("display", Tag.TAG_COMPOUND)) {
            CompoundTag display = tag.getCompound("display");
            savedLore = display.contains("Lore", Tag.TAG_LIST) ? display.getList("Lore", Tag.TAG_STRING).copy() : null;
        } else {
            savedLore = null;
        }
    }
}
