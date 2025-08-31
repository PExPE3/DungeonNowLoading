package dev.hexnowloading.dungeonnowloading.supporter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DNLCheckpointHeads {
    // Use the same style you had; the simpler canonical raw URL also works:
    // https://raw.githubusercontent.com/hexnowloading/DungeonNowLoadingSupporterList/main/checkpoint_heads/temple_of_duality.json
    private static final String GITHUB_URL =
            "https://raw.githubusercontent.com/hexnowloading/DungeonNowLoadingSupporterList/refs/heads/main/checkpoint_heads/temple_of_duality.json";

    // Parsed pool
    private static volatile Pool POOL;

    // --- API -----------------------------------------------------------------

    /** Call once at startup (both sides). Safe to call multiple times. */
    public static void loadTempleHeads() {
        try {
            URL url = new URL(GITHUB_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            try (Reader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                POOL = parsePool(root);
                System.out.println("[DNL] Loaded checkpoint head pool: "
                        + (POOL.heads.size()) + " entries, default_weight=" + POOL.defaultWeight);
            }
        } catch (Exception e) {
            System.err.println("[DNL] Failed to load checkpoint heads: " + e.getMessage());
        }
    }

    /** Returns true if a pool is loaded. */
    public static boolean isLoaded() { return POOL != null && !POOL.heads.isEmpty(); }

    /**
     * Pick a weighted random head for Temple of Duality.
     * Returns null if pool not loaded or no eligible heads.
     */
    public static ItemStack pickTempleHead(RandomSource rand) {
        if (!isLoaded()) return ItemStack.EMPTY;

        // Weighted roulette
        int total = 0;
        for (Head h : POOL.heads) total += Math.max(1, h.weightOrDefault(POOL.defaultWeight));
        if (total <= 0) return ItemStack.EMPTY;

        int r = rand.nextInt(total);
        Head chosen = null;
        for (Head h : POOL.heads) {
            int w = Math.max(1, h.weightOrDefault(POOL.defaultWeight));
            if (r < w) { chosen = h; break; }
            r -= w;
        }
        if (chosen == null) return ItemStack.EMPTY;
        return toItemStack(chosen);
    }

    // --- Data model ----------------------------------------------------------

    private static Pool parsePool(JsonObject root) {
        int defaultWeight = root.has("default_weight") ? Math.max(1, root.get("default_weight").getAsInt()) : 1;
        List<Head> heads = new ArrayList<>();

        JsonArray arr = root.getAsJsonArray("heads");
        if (arr != null) {
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();

                UUID uuid = o.has("uuid") ? UUID.fromString(o.get("uuid").getAsString()) : null;
                String username = o.has("username") ? o.get("username").getAsString() : null;

                int weight = o.has("weight") ? o.get("weight").getAsInt() : -1;

                // structures (optional) – we still keep only temple entries; your file already scopes it.
                List<String> structures = new ArrayList<>();
                if (o.has("structures") && o.get("structures").isJsonArray()) {
                    for (JsonElement se : o.getAsJsonArray("structures")) structures.add(se.getAsString());
                }

                // cosmetics (array of strings)
                List<String> cosmetics = new ArrayList<>();
                if (o.has("cosmetics") && o.get("cosmetics").isJsonArray()) {
                    for (JsonElement ce : o.getAsJsonArray("cosmetics")) cosmetics.add(ce.getAsString());
                }

                // display { name: JsonObject, lore: JsonArray of JsonObject }
                JsonObject display = o.has("display") ? o.getAsJsonObject("display") : null;
                JsonObject name = (display != null && display.has("name")) ? display.getAsJsonObject("name") : null;
                JsonArray  lore = (display != null && display.has("lore")) ? display.getAsJsonArray("lore") : null;

                heads.add(new Head(uuid, username, weight, structures, cosmetics, name, lore));
            }
        }

        return new Pool(defaultWeight, Collections.unmodifiableList(heads));
    }

    // Minimal pool classes
    private record Pool(int defaultWeight, List<Head> heads) {}

    private record Head(UUID uuid,
                        String username,
                        int weight, // -1 means “use pool default”
                        List<String> structures,
                        List<String> cosmetics,
                        JsonObject name,
                        JsonArray lore) {
        int weightOrDefault(int def) { return weight > 0 ? weight : def; }
    }

    // --- ItemStack factory ---------------------------------------------------

    private static ItemStack toItemStack(Head h) {
        ItemStack stack = new ItemStack(dev.hexnowloading.dungeonnowloading.registry.DNLItems.CHECKPOINT_HEAD.get());
        CompoundTag root = stack.getOrCreateTag();

        // 1) Prefer the simple vanilla string SkullOwner for the ITEM (what you want)
        if (h.username != null && !h.username.isEmpty()) {
            root.putString("SkullOwner", h.username);
        } else if (h.uuid != null) {
            // Fallback only if no username available: you can either
            //   (A) skip owner (vanilla will show default skin until placed and resolved), or
            //   (B) write a compound with Id (stable but not the format you want).
            // If you must set something, uncomment (B) below.
            // GameProfile profile = new GameProfile(h.uuid, null);
            // root.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
        }

        // (Optional) Keep UUID for your own logic without affecting vanilla owner format
        if (h.uuid != null) root.putUUID("DNL_OwnerUuid", h.uuid);

        // 2) Cosmetics list + default index 0
        if (!h.cosmetics.isEmpty()) {
            ListTag list = new ListTag();
            for (String id : h.cosmetics) list.add(StringTag.valueOf(id));
            root.put("DNL_Cosmetics", list);
            root.putInt("DNL_CosmeticIdx", 0);
        }

        // 3) Display: raw JSON strings
        CompoundTag disp = stack.getOrCreateTagElement("display");
        if (h.name != null) disp.putString("Name", h.name.toString());
        if (h.lore != null) {
            ListTag loreList = new ListTag();
            for (JsonElement je : h.lore) loreList.add(StringTag.valueOf(je.toString()));
            disp.put("Lore", loreList);
        }

        return stack;
    }
}
