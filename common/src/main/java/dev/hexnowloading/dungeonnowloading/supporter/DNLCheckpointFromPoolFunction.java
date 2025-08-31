package dev.hexnowloading.dungeonnowloading.supporter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DNLCheckpointFromPoolFunction extends LootItemConditionalFunction {
    public static final LootItemFunctionType TYPE = new LootItemFunctionType(new Serializer());

    protected DNLCheckpointFromPoolFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static Builder<?> builder() {
        return simpleBuilder(DNLCheckpointFromPoolFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack original, LootContext ctx) {
        // lazy-load (safe to call repeatedly)
        if (!DNLCheckpointHeads.isLoaded()) DNLCheckpointHeads.loadTempleHeads();
        ItemStack picked = DNLCheckpointHeads.pickTempleHead(ctx.getRandom());
        return picked.isEmpty() ? original : picked;
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }

    // --- Serializer (no custom fields, just conditions) ---
    public static class Serializer extends LootItemConditionalFunction.Serializer<DNLCheckpointFromPoolFunction> {
        @Override
        public DNLCheckpointFromPoolFunction deserialize(JsonObject obj, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
            return new DNLCheckpointFromPoolFunction(conditions);
        }
        @Override
        public void serialize(JsonObject obj, DNLCheckpointFromPoolFunction fn, JsonSerializationContext ctx) {
            // no extra fields; conditions handled by super
        }
    }
}