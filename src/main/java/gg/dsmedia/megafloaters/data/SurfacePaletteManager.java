package gg.dsmedia.megafloaters.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import gg.dsmedia.megafloaters.MegaFloatersMod;
import gg.dsmedia.megafloaters.api.palette.PaletteCodecs;
import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Datapack-driven palette overrides.
 *
 * <ul>
 *   <li>{@code data/<ns>/megafloaters/palettes/<biome>.json} — replaces the
 *       built-in palette for {@code <ns>:<biome>}.</li>
 *   <li>{@code data/<ns>/megafloaters/palettes/<biome>/<archetype>.json} —
 *       replaces the palette for that biome + archetype combination (wins
 *       over the biome-only override).</li>
 * </ul>
 *
 * <p>A datapack doesn't have to ship both flavours; the registry's built-in
 * defaults remain the fallback.
 */
public class SurfacePaletteManager extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "megafloaters/palettes";
    private static final Gson GSON = new Gson();

    private static volatile SurfacePaletteManager instance;

    private Map<ResourceLocation, SurfacePalette> biomeOverrides = Map.of();
    private Map<String, SurfacePalette> biomeArchetypeOverrides = Map.of();

    public SurfacePaletteManager() {
        super(GSON, DIRECTORY);
        instance = this;
    }

    public static Optional<SurfacePalette> lookup(ResourceLocation biome, String archetypePath) {
        SurfacePaletteManager inst = instance;
        if (inst == null) return Optional.empty();
        SurfacePalette specific = inst.biomeArchetypeOverrides.get(biomeArchetypeKey(biome, archetypePath));
        if (specific != null) return Optional.of(specific);
        return Optional.ofNullable(inst.biomeOverrides.get(biome));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager,
                         ProfilerFiller profiler) {
        Map<ResourceLocation, SurfacePalette> biomeMap = new HashMap<>();
        Map<String, SurfacePalette> biomeArchetypeMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation key = entry.getKey();
            String path = key.getPath();
            int slash = path.lastIndexOf('/');
            var parsed = PaletteCodecs.PALETTE.parse(JsonOps.INSTANCE, entry.getValue());
            if (parsed.error().isPresent()) {
                MegaFloatersMod.LOGGER.warn("Invalid palette JSON at {}: {}", key,
                        parsed.error().get().message());
                continue;
            }
            SurfacePalette palette = parsed.result().orElseThrow();

            if (slash < 0) {
                biomeMap.put(key, palette);
            } else {
                ResourceLocation biome = ResourceLocation.fromNamespaceAndPath(
                        key.getNamespace(), path.substring(0, slash));
                String archetype = path.substring(slash + 1);
                biomeArchetypeMap.put(biomeArchetypeKey(biome, archetype), palette);
            }
        }

        this.biomeOverrides = Map.copyOf(biomeMap);
        this.biomeArchetypeOverrides = Map.copyOf(biomeArchetypeMap);
        MegaFloatersMod.LOGGER.info("Loaded {} biome palette override(s), {} biome+archetype override(s).",
                biomeMap.size(), biomeArchetypeMap.size());
    }

    private static String biomeArchetypeKey(ResourceLocation biome, String archetypePath) {
        return biome + "#" + archetypePath;
    }
}
