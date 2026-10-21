package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

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

    /** Each step's still and flowing sprite, published whole: chunk-building threads read and set it. */
    private static volatile TextureAtlasSprite[][] sprites;

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

    /**
     * Each step's still and flowing sprite, looked up again whenever the block atlas has been rebuilt: the atlas stays
     * the same object for the whole game, and a reload swaps its sprites, so the first sprite is what tells.
     */
    private static TextureAtlasSprite[][] stepSprites() {
        TextureAtlas blocks = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite[][] kept = sprites;
        if (kept == null || blocks.getSprite(STILL[0]) != kept[0][0]) {
            TextureAtlasSprite[][] found = new TextureAtlasSprite[STEPS][];
            for (int i = 0; i < STEPS; i++) {
                found[i] = new TextureAtlasSprite[] {blocks.getSprite(STILL[i]), blocks.getSprite(FLOWING[i])};
            }
            sprites = found;
            kept = found;
        }
        return kept;
    }

    /**
     * A sprite made from {@code original}'s pixels as {@code change} turns them, under the name {@code sprite}, keeping its
     * animation, as its metadata goes along. From 1.21.2 the image goes straight into the sprite: NativeImage can no
     * longer write itself back out as a PNG for the atlas to read again.
     */
    static SpriteSource.SpriteSupplier madeFrom(ResourceLocation sprite, Resource original, Consumer<NativeImage> change) {
        return loader -> {
            ResourceMetadata metadata;
            FrameSize frame;
            NativeImage image;
            try (InputStream in = original.open()) {
                metadata = original.metadata();
                image = NativeImage.read(in);
                frame = metadata.getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY)
                        .calculateFrameSize(image.getWidth(), image.getHeight());
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Can't read the texture to make {} from", sprite, e);
                return null;
            }
            if (!Mth.isMultipleOf(image.getWidth(), frame.width()) || !Mth.isMultipleOf(image.getHeight(), frame.height())) {
                LOGGER.error("The texture to make {} from is {}x{}, not a multiple of its frame", sprite, image.getWidth(),
                        image.getHeight());
                image.close();
                return null;
            }
            change.accept(image);
            return new SpriteContents(sprite, frame, image, metadata);
        };
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
            output.add(sprite, madeFrom(sprite, found.get(), this::murkier));
        }

        private void murkier(NativeImage image) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getPixel(x, y);
                    int alpha = argb >>> 24;
                    if (alpha == 0) {
                        continue;
                    }
                    int a = Math.round(alpha + (255 - alpha) * opacity);
                    int r = Math.round((argb >> 16 & 0xFF) * shade);
                    int g = Math.round((argb >> 8 & 0xFF) * shade);
                    int b = Math.round((argb & 0xFF) * shade);
                    image.setPixel(x, y, a << 24 | r << 16 | g << 8 | b);
                }
            }
        }

        @Override
        public SpriteSourceType type() {
            return type;
        }
    }
}
