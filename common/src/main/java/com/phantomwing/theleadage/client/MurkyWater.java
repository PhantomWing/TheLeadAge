package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
 * Mojang's art ships in the jar. Each loader registers the source's type and hands this class the water model it is
 * about to draw a block with: 26.1 draws a fluid from a {@link FluidModel} of its sprites, one for all the water, so each
 * loader's {@code FluidRendererMixin} swaps in a copy of it with the murky sprites, made in that loader's own way, since
 * NeoForge's FluidModel carries more than vanilla's.</p>
 */
public final class MurkyWater {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath(TheLeadAge.MOD_ID, "murky_water");
    private static final int STEPS = 3;
    private static final Identifier[] STILL = new Identifier[STEPS];
    private static final Identifier[] FLOWING = new Identifier[STEPS];

    static {
        for (int i = 0; i < STEPS; i++) {
            STILL[i] = Identifier.fromNamespaceAndPath(TheLeadAge.MOD_ID, "block/murky_water_still_" + (i + 1));
            FLOWING[i] = Identifier.fromNamespaceAndPath(TheLeadAge.MOD_ID, "block/murky_water_flow_" + (i + 1));
        }
    }

    public static final MapCodec<Source> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("source").forGetter(Source::source),
            Identifier.CODEC.fieldOf("sprite").forGetter(Source::sprite),
            Codec.floatRange(0.0F, 1.0F).fieldOf("opacity").forGetter(Source::opacity),
            Codec.floatRange(0.0F, 1.0F).fieldOf("shade").forGetter(Source::shade)
    ).apply(instance, Source::new));

    /** Each step's still and flowing sprite, published whole: chunk-building threads read and set it. */
    private static volatile TextureAtlasSprite[][] sprites;
    /** Each step's copy of the water model last asked about, published whole as {@link #sprites} is. */
    private static volatile Models models;

    private MurkyWater() {
    }

    /**
     * The model water at {@code pos} is drawn with: {@code model} with its still and flowing sprites turned to the murky
     * ones where the water is contaminated, else {@code model} itself. Called for every block of water a chunk draws.
     */
    public static FluidModel model(BlockPos pos, FluidState state, FluidModel model, Copier copier) {
        if (!state.is(FluidTags.WATER)) {
            return model;
        }
        float strength = LeadWaterTint.strength(pos);
        if (strength <= 0.0F) {
            return model;
        }
        TextureAtlasSprite[][] steps = stepSprites();
        Models kept = models;
        if (kept == null || kept.source() != model || kept.sprites() != steps) {
            FluidModel[] made = new FluidModel[STEPS];
            for (int i = 0; i < STEPS; i++) {
                made[i] = copier.copy(model,
                        new Material.Baked(steps[i][0], model.stillMaterial().forceTranslucent()),
                        new Material.Baked(steps[i][1], model.flowingMaterial().forceTranslucent()));
            }
            kept = new Models(model, steps, made);
            models = kept;
        }
        return kept.steps()[Mth.clamp(Mth.ceil(strength * STEPS), 1, STEPS) - 1];
    }

    /** Makes a copy of a fluid model with other still and flowing sprites, as the loader's FluidModel is made. */
    @FunctionalInterface
    public interface Copier {
        FluidModel copy(FluidModel model, Material.Baked still, Material.Baked flowing);
    }

    /** The water model each step's copy was made from, the sprites it was made with, and the copies. */
    private record Models(FluidModel source, TextureAtlasSprite[][] sprites, FluidModel[] steps) {
    }

    /**
     * Each step's still and flowing sprite, looked up again whenever the block atlas has been rebuilt: the atlas stays
     * the same object for the whole game, and a reload swaps its sprites, so the first sprite is what tells.
     */
    private static TextureAtlasSprite[][] stepSprites() {
        TextureAtlas blocks = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
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
     * longer write itself back out as a PNG for the atlas to read again. From 1.21.11 the sprite takes the texture section
     * (blur, clamp, mipmaps) too, as vanilla's own loader gives it.
     */
    static SpriteSource.DiscardableLoader madeFrom(Identifier sprite, Resource original, Consumer<NativeImage> change) {
        return loader -> {
            Optional<AnimationMetadataSection> animation;
            Optional<TextureMetadataSection> texture;
            FrameSize frame;
            NativeImage image;
            try (InputStream in = original.open()) {
                ResourceMetadata metadata = original.metadata();
                // 1.21.9: the sprite takes the animation section itself rather than the whole metadata.
                animation = metadata.getSection(AnimationMetadataSection.TYPE);
                texture = metadata.getSection(TextureMetadataSection.TYPE);
                image = NativeImage.read(in);
                // 1.21.4: no EMPTY section to fall back on; a texture without one is a single frame, as SpriteLoader has it.
                int width = image.getWidth(), height = image.getHeight();
                frame = animation.map(section -> section.calculateFrameSize(width, height))
                        .orElseGet(() -> new FrameSize(width, height));
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
            // No other sections: those an atlas asks for (a GUI sprite's scaling) mean nothing to water or a particle.
            return new SpriteContents(sprite, frame, image, animation, List.of(), texture);
        };
    }

    /**
     * A texture made from another as the atlas loads: {@code source}'s pixels, {@code opacity} of the way from their own
     * alpha to fully opaque, their colour darkened to {@code shade}, under the name {@code sprite}. The source's
     * animation goes along, as its metadata does.
     */
    public record Source(Identifier source, Identifier sprite, float opacity, float shade) implements SpriteSource {
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
        public MapCodec<Source> codec() {
            return CODEC;
        }
    }
}
