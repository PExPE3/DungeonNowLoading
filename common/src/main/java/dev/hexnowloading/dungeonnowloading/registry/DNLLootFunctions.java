package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.supporter.DNLCheckpointFromPoolFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.function.Supplier;

public final class DNLLootFunctions {
    @SuppressWarnings("unchecked")
    public static final Supplier<LootItemFunctionType> CHECKPOINT_FROM_POOL =
            Services.REGISTRY.register(
                    (Registry<LootItemFunctionType>) BuiltInRegistries.LOOT_FUNCTION_TYPE, // <-- Registry<? super T>
                    "checkpoint_from_pool",                                                // <-- plain name, helper will add modid
                    () -> DNLCheckpointFromPoolFunction.TYPE                               // <-- your type instance
            );

    public static void bootstrap() { /* no-op; call once in common init if needed */ }
}
