package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Water raw lead contaminates, drawn less see-through than clean water: a tint only colours water, whose transparency is
 * its texture's, so contaminated water is drawn with textures of its own, more opaque and darker the stronger it is -
 * three steps, a block each where it fades into clean water ({@link LeadWaterTint#strength}). Shader packs that keep the water's texture,
 * as Complementary's default water does, show it too.
 *
 * <p>The textures are made when the block atlas loads, from whichever water texture is in use - vanilla's or a resource
 * pack's - by the {@link Source} the atlas file {@code assets/minecraft/atlases/blocks.json} lists, so no copy of
 * Mojang's art ships in the jar. Each loader registers the source's type and hands this class the water sprites it is
 * about to draw: NeoForge's {@code FluidSpriteCacheMixin}, which Sodium goes through too, and Fabric's water
 * {@code FluidRenderHandler}.</p>
 */
public final class MurkyWater {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation TYPE_ID = ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "murky_water");
    private static final int STEPS = 3;
    private static final ResourceLocation[] STILL = new ResourceLocation[STEPS];
    private static final ResourceLocation[] FLOWING = new ResourceLocation[STEPS];

    static {
        for (int i = 0; i < STEPS; i++) {
            STILL[i] = ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "block/murky_water_still_" + (i + 1));
            FLOWING[i] = ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "block/murky_water_flow_" + (i + 1));
        }
    }

    public static final MapCodec<Source> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("source").forGetter(Source::source),
            ResourceLocation.CODEC.fieldOf("sprite").forGetter(Source::sprite),
            Codec.floatRange(0.0F, 1.0F).fieldOf("opacity").forGetter(Source::opacity),
            Codec.floatRange(0.0F, 1.0F).fieldOf("shade").forGetter(Source::shade)
    ).apply(instance, Source::new));

    /** The registered type of {@link Source}, set by the loader that registered it. */
    @Nullable
    public static SpriteSourceType type;

    private static TextureAtlas atlas;
    private static TextureAtlasSprite[][] sprites;

    private MurkyWater() {
    }

    /**
     * The sprites water at {@code pos} is drawn with - still, flowing and the overlay - turned to the murky ones where it is
     * contaminated, or null to keep {@code original}. Called for every block of water a chunk draws.
     */
    @Nullable
    public static TextureAtlasSprite[] sprites(BlockPos pos, FluidState state, TextureAtlasSprite[] original) {
        if (original.length < 2 || !state.is(FluidTags.WATER)) {
            return null;
        }
        float strength = LeadWaterTint.strength(pos);
        if (strength <= 0.0F) {
            return null;
        }
        TextureAtlasSprite[] step = stepSprites()[Mth.clamp(Mth.ceil(strength * STEPS), 1, STEPS) - 1];
        TextureAtlasSprite[] murky = original.clone();
        murky[0] = step[0];
        murky[1] = step[1];
        return murky;
    }

    /** Each step's still and flowing sprite, looked up again whenever the block atlas has been rebuilt. */
    private static TextureAtlasSprite[][] stepSprites() {
        TextureAtlas blocks = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        if (blocks != atlas || sprites == null) {
            TextureAtlasSprite[][] found = new TextureAtlasSprite[STEPS][];
            for (int i = 0; i < STEPS; i++) {
                found[i] = new TextureAtlasSprite[] {blocks.getSprite(STILL[i]), blocks.getSprite(FLOWING[i])};
            }
            sprites = found;
            atlas = blocks;
        }
        return sprites;
    }

    /**
     * A texture made from another as the atlas loads: {@code source}'s pixels, {@code opacity} of the way from their own
     * alpha to fully opaque, their colour darkened to {@code shade}, under the name {@code sprite}. The source's
     * animation goes along, as its metadata does.
     */
    public record Source(ResourceLocation source, ResourceLocation sprite, float opacity, float shade) implements SpriteSource {
        @Override
        public void run(ResourceManager manager, SpriteSource.Output output) {
            Optional<Resource> found = manager.getResource(TEXTURE_ID_CONVERTER.idToFile(source));
            if (found.isEmpty()) {
                LOGGER.warn("No texture {} to make the murky water texture {} from", source, sprite);
                return;
            }
            Resource original = found.get();
            output.add(sprite, new Resource(original.source(), () -> murkier(original), original::metadata));
        }

        private InputStream murkier(Resource original) throws IOException {
            try (InputStream in = original.open(); NativeImage image = NativeImage.read(in)) {
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int abgr = image.getPixelRGBA(x, y);
                        int alpha = abgr >>> 24;
                        if (alpha == 0) {
                            continue;
                        }
                        int a = Math.round(alpha + (255 - alpha) * opacity);
                        int b = Math.round((abgr >> 16 & 0xFF) * shade);
                        int g = Math.round((abgr >> 8 & 0xFF) * shade);
                        int r = Math.round((abgr & 0xFF) * shade);
                        image.setPixelRGBA(x, y, a << 24 | b << 16 | g << 8 | r);
                    }
                }
                return new ByteArrayInputStream(image.asByteArray());
            }
        }

        @Override
        public SpriteSourceType type() {
            return type;
        }
    }
}
