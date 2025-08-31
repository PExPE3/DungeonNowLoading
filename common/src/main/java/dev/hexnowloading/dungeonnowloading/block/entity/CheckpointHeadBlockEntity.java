package dev.hexnowloading.dungeonnowloading.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CheckpointHeadBlockEntity extends SkullBlockEntity {
    @Nullable private ListTag savedLore;     // JSON strings
    @Nullable private String  savedNameJson; // raw JSON string
    @Nullable String cosmeticId;

    public CheckpointHeadBlockEntity(BlockPos pos, BlockState state) { super(pos, state); }
    public CheckpointHeadBlockEntity(BlockPos pos, BlockState state, GameProfile owner) {
        super(pos, state);
        setOwner(owner);
    }

    /** Read Name/Lore from the item on placement (cache raw JSON) */
    public void setDisplayFromItem(ItemStack stack) {
        CompoundTag display = stack.getTagElement("display");
        if (display != null) {
            savedLore = display.contains("Lore", Tag.TAG_LIST)
                    ? display.getList("Lore", Tag.TAG_STRING).copy()
                    : null;
            savedNameJson = display.contains("Name", Tag.TAG_STRING)
                    ? display.getString("Name")
                    : null;
        } else {
            savedLore = null;
            savedNameJson = null;
        }
        setChanged();
    }

    /** Write cached Name/Lore back to an item (pick-block or manual drops) */
    public void writeDisplayToItem(ItemStack stack) {
        CompoundTag disp = stack.getOrCreateTagElement("display");
        if (savedLore != null && !savedLore.isEmpty()) {
            disp.put("Lore", savedLore.copy());
        }
        if (savedNameJson != null && !savedNameJson.isBlank()) {
            disp.putString("Name", savedNameJson);
        }
    }

    // Persist under the SAME nesting ("display") so loot copy_nbt can copy it back.
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag display = tag.getCompound("display");
        if (savedLore != null && !savedLore.isEmpty()) {
            display.put("Lore", savedLore.copy());
        }
        if (savedNameJson != null && !savedNameJson.isBlank()) {
            display.putString("Name", savedNameJson);
        }
        if (!display.isEmpty()) tag.put("display", display);
        if (cosmeticId != null && !cosmeticId.isBlank()) tag.putString("DNL_Cosmetic", cosmeticId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("display", Tag.TAG_COMPOUND)) {
            CompoundTag display = tag.getCompound("display");
            savedLore = display.contains("Lore", Tag.TAG_LIST)
                    ? display.getList("Lore", Tag.TAG_STRING).copy()
                    : null;
            savedNameJson = display.contains("Name", Tag.TAG_STRING)
                    ? display.getString("Name")
                    : null;
        } else {
            savedLore = null;
            savedNameJson = null;
        }
        cosmeticId = tag.contains("DNL_Cosmetic", Tag.TAG_STRING) ? tag.getString("DNL_Cosmetic") : null;

    }

    /** Build: [Username] message  (name uses item-name color, message uses lore color) */
    @javax.annotation.Nullable
    public Component buildRightClickLine() {
        Component msg = parseFirstChatLoreOrNull();
        if (msg == null) {
            String owner = getOwnerProfile() != null ? getOwnerProfile().getName() : "Unknown";
            return Component.literal("[" + owner + "] ").append(Component.literal("..."))
                    .withStyle(s -> s.withItalic(false));
        }

        // --- name color from saved Name JSON (fallback white) ---
        int nameRgb = 0xFFFFFF;
        if (savedNameJson != null && !savedNameJson.isBlank()) {
            try {
                Component nameC = Component.Serializer.fromJson(savedNameJson);
                var col = nameC != null ? nameC.getStyle().getColor() : null;
                if (col != null) nameRgb = col.getValue();
            } catch (Exception ignored) {}
        }

        // --- message color from lore JSON (fallback aqua-ish) ---
        int msgRgb = 0x55FFFF;
        var mc = msg.getStyle().getColor();
        if (mc != null) msgRgb = mc.getValue();

        Style nameStyle = Style.EMPTY.withColor(TextColor.fromRgb(nameRgb)).withItalic(false);
        Style msgStyle  = Style.EMPTY.withColor(TextColor.fromRgb(msgRgb)).withItalic(false);

        String owner = getOwnerProfile() != null ? getOwnerProfile().getName() : "Unknown";
        MutableComponent name = Component.literal("[" + owner + "] ").withStyle(nameStyle);
        return Component.empty().append(name).append(msg.copy().withStyle(msgStyle));
    }

    @javax.annotation.Nullable
    private Component parseFirstChatLoreOrNull() {
        if (savedLore == null || savedLore.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        TextColor firstColor = null;
        boolean any = false;

        for (int i = 0; i < savedLore.size(); i++) {
            String raw = savedLore.getString(i);

            // Skip non-chat lines (role, structure, spacer, etc.)
            if (raw.contains("\"dnl_skip_chat\":true")) continue;

            Component c;
            try {
                c = Component.Serializer.fromJson(raw);
                if (c == null) continue;
            } catch (Exception e) {
                c = Component.literal(raw);
            }

            String plain = c.getString();
            if (plain.trim().isEmpty()) continue;               // empty/spacer
            if (plain.trim().endsWith(" Patron")) continue;     // role line safety

            // Clean this chunk:
            String piece = plain.replaceFirst("^\\s+", "");     // drop leading spaces
            if (!any && piece.startsWith("\"")) {               // drop opening quote on first chunk
                piece = piece.substring(1);
            }
            if (piece.isEmpty()) continue;

            // Append with single space between chunks
            if (sb.length() > 0) sb.append(' ');
            sb.append(piece);

            // Remember color from the first real chunk
            if (!any) {
                TextColor col = c.getStyle().getColor();
                if (col != null) firstColor = col;
                any = true;
            }
        }

        if (!any) return null;

        // Drop trailing quote, if present
        int len = sb.length();
        if (len > 0 && sb.charAt(len - 1) == '\"') {
            sb.setLength(len - 1);
        }

        MutableComponent out = Component.literal(sb.toString());
        if (firstColor != null) {
            // no lambda capture; just set the style directly
            out.setStyle(out.getStyle().withColor(firstColor));
        }
        return out;
    }

    public void setCosmeticFromItem(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null && tag.contains("DNL_Cosmetic", Tag.TAG_STRING)) {
            String id = tag.getString("DNL_Cosmetic").trim();
            cosmeticId = id.isEmpty() ? null : id;
            setChanged();
        }
    }

    // --- BE -> item on drop/pick ---
    public void writeCosmeticToItem(ItemStack stack) {
        if (cosmeticId != null && !cosmeticId.isBlank()) {
            stack.getOrCreateTag().putString("DNL_Cosmetic", cosmeticId);
        }
    }

    @Nullable public String getCosmeticId() { return cosmeticId; }
    public boolean hasCosmetic() { return cosmeticId != null && !cosmeticId.isBlank(); }
}
