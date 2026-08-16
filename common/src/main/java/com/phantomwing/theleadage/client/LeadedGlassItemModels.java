package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

/**
 * Loader-agnostic client registration for the leaded glass item models.
 *
 * <p>1.21.4 removed item-model {@code overrides}, item color handlers and BEWLR: the pane, door
 * and trapdoor icons are data-driven {@code items/*.json} definitions backed by two custom special
 * renderers ({@code theleadage:leaded_glass_pane}, {@code theleadage:leaded_glass_trapdoor}) that
 * draw the design from the stack's config. Their types live in vanilla's private late-bound
 * {@code SpecialModelRenderers.ID_MAPPER}, widened via the access widener (+ NeoForge AT mirror)
 * so registration is byte-identical on both loaders.</p>
 */
@Environment(EnvType.CLIENT)
public final class LeadedGlassItemModels {
    public static final ResourceLocation TRAPDOOR_SPECIAL_ID = TheLeadAge.resourceLocation("leaded_glass_trapdoor");
    public static final ResourceLocation PANE_SPECIAL_ID = TheLeadAge.resourceLocation("leaded_glass_pane");

    private LeadedGlassItemModels() {
    }

    /** Client-setup registrations shared by both loaders; also usable from datagen (serialization side). */
    public static void registerTypes() {
        SpecialModelRenderers.ID_MAPPER.put(TRAPDOOR_SPECIAL_ID, LeadedGlassTrapdoorSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(PANE_SPECIAL_ID, LeadedGlassPaneItemSpecialRenderer.Unbaked.MAP_CODEC);
    }

    /**
     * Render layers for the transparent blocks. 1.21.4 dropped the model-JSON render_type our old
     * datagen emitted, so the layers are registered in code (covers both loaders; replaces the
     * Fabric-only ModRenderLayers).
     */
    public static void registerRenderLayers() {
        RenderTypeRegistry.register(RenderType.cutout(), CUTOUT_BLOCKS);
        RenderTypeRegistry.register(RenderType.translucent(), TRANSLUCENT_BLOCKS);
    }

    private static final Block[] CUTOUT_BLOCKS = Stream.concat(
            Stream.of(ModBlocks.LEADED_GLASS, ModBlocks.LEAD_GRATE, ModBlocks.LEAD_CHAIN, ModBlocks.LEAD_BARS,
                    ModBlocks.LEAD_TORCH, ModBlocks.LEAD_WALL_TORCH, ModBlocks.LEAD_LANTERN,
                    ModBlocks.LEAD_DOOR, ModBlocks.LEAD_TRAPDOOR,
                    ModBlocks.LEADED_GLASS_DOOR, ModBlocks.LEADED_GLASS_TRAPDOOR,
                    ModBlocks.LEAD_WEIGHT, ModBlocks.CHIPPED_LEAD_WEIGHT, ModBlocks.DAMAGED_LEAD_WEIGHT),
            Stream.of(ModBlocks.LEADED_GLASS_PANEL, ModBlocks.LEADED_GLASS_PANE_SPLIT,
                    ModBlocks.LEADED_GLASS_PANE_PLUS, ModBlocks.LEADED_GLASS_PANE_GRID,
                    ModBlocks.LEADED_GLASS_PANE_DIAGONAL, ModBlocks.LEADED_GLASS_PANE_CROSS,
                    ModBlocks.LEADED_GLASS_PANE_DIAMOND, ModBlocks.LEADED_GLASS_PANE_LATTICE,
                    ModBlocks.LEADED_GLASS_PANE_BARS, ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS)
    ).map(s -> (Block) s.get()).toArray(Block[]::new);

    private static final Block[] TRANSLUCENT_BLOCKS = Stream.of(DyeColor.values())
            .map(c -> (Block) ModBlocks.STAINED_LEADED_GLASS.get(c).get()).toArray(Block[]::new);

}
